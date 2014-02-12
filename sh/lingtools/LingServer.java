/* ============================
  LingServer
    API (in progress)
	2014-01-15
	a socket server listens to requests from Nodejs
	nodejs passes in a sentence form the web page...
	on connection LingServer:
	- grabs the input; 
	- starts an intance of ambiguity detector on a thread

	on instantiation LingSever:
		- preloads a (serailzed) WEKA 'logitBoost' Classifier for the ambiguity Detector.
		- preloads the (serialized) traingDataSet that was used to train the pre-loaded Classifier.
			- the dataset is used to align the testData to the trainedData as required by weka.		
		- preloads the wordnet dictionaries.
  
  
  =============================== */



// threading 
import java.net.*;
import java.lang.Thread;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
 
import java.lang.SecurityException;
import java.util.*;
import java.io.*;

// heuristics 
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import shef.nlp.wordnet.similarity.SimilarityInfo;
import shef.nlp.wordnet.similarity.SimilarityMeasure;
import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.meta.LogitBoost;
 
 
public class LingServer {

  // unused currently
  interface LingServerBroadcast{  public void broadcast(String string); }
  private ServerSocket serverSocket; // the socket, kept open, on which this server listens for connections from Nodejs.
  private ExecutorService thrdpool;  // threadpool used with callable interface. 'callable' JAVA threads can return results.
  public static int thrdcount = 0;	 // used to pass unique names to threads

  // the trained WEKA.Classifier must be instantited before calling coordAmbiguity.heuristics().
  public static TrainingClassifier ambiclassifier;  	// trained classifier wrapper implements callable, various methods...
  public static Classifier logitclassifier;   			// trained classifier - called from coordAmbiguityProc
  public static Instances trainingSet;					// training data used for this classifier -align testData before submitting to Classifier.
  public static SimilarityMeasure LSsim;				// preload the wordnet dictionaries and create a SimilartyMeasure object for the ambiguity heuristics process.
  
  public static boolean JWNLpreload = false;
  
  // no-json data delims -  UTF8 safe and efficient 
	public static final String STX = Character.toString ((char) 2); //start of text
	public static final String ETX = Character.toString ((char) 3); //end of text
	public static final String GS = Character.toString ((char) 29); //group seperatorpublic static final 
	public static final String RS = Character.toString ((char) 30); //record seperator
    public static final String US = Character.toString ((char) 31); //unit seperator
       
  
  public LingServer(int port) throws IOException,Exception
    {
      serverSocket = new ServerSocket(port);
	  thrdpool = Executors.newFixedThreadPool(3);  //
      //serverSocket.setSoTimeout(10000);
    }
 
 
 //  Nodejs routes HTTP POSTs to the listening port to a node 'net' socket handler passing in the POST paylod - a text sentence for Ling detection.
 //  node's socket handler requests a connection to the LingServer and passes the text out to the JAVA LingServer.
 //  The LingServer ServerSocket listens for connections from nodejs.
 //  when LingServer ServerSocket accepts a connection from node it assigns nodejs a unique port to listen back on.
 //  nodejs RequestHandler.coordambiguitysock() assigns a closure/callback to each net.connect 'client'
 //  (javascript sees fuctions as 1st-class objects). 
 //  The closure of the Node's connecting object is bound to that connection.
 //  So when the client recieves a data event from the LingServer the handler is already bound to the specific HTTP request/response and is correctyl routed back to the
//   requesting user-agent (webpage in this case).
 public void listen()  {
      while(true)
      {
         try
         {
            System.out.println("LingServer waiting for connection on port " + serverSocket.getLocalPort() + "...");
			//TODO: screen IP/Host - use security-manager checkAccept() method?
			//TODO:  set Keepalive? Close socket or reuse? 
			//TODO:  setReuseAddress, obviate connection timeouts on this socket?
			//TODO: close() and reopen all sockets?
            Socket socket = serverSocket.accept();
			System.out.println("LingServer connected to "  + socket.getRemoteSocketAddress() + ":" + socket.getLocalSocketAddress());
    		BufferedReader is = new BufferedReader( new InputStreamReader (socket.getInputStream()));
			PrintStream os = new PrintStream(socket.getOutputStream());
			String text = is.readLine();
			String output = "";
			List<Future<coordAmbiguityResults>> resultset = new ArrayList<Future<coordAmbiguityResults>>(); //ArrayList<Future<List<String>>>();
			System.out.println("LingServer data received: " + text);
			try{
				// object implements runnable. 
				// Obj declares and instantiates a SketchEngineQueryObj member.
				// Runnable Object's Thread.start() called from obj constructor. 
				// obj.run() calls sketchengine.logon().
				// sketchengine.logon() intialize sketchengine HTTPClient 'client'.
				// sketchengine.runQuery() uses client to connect to SketchEngine socket.  
				//coordAmbiguityProc ambithrd = new coordAmbiguityProc("coordAmbiguity", text);
				//callable
				// 
				thrdcount++;
				System.out.println("LingServer instantiates coordAmbiguityProc object.");
				Callable<coordAmbiguityResults>  ambithrd = new coordAmbiguityProc("ambithrd_"+thrdcount, text);
				System.out.println("LingServer threads out coordAmbiguityProc object.");
				Future<coordAmbiguityResults> ambiworker = thrdpool.submit(ambithrd);
				System.out.println("LingServer adds coordAmbiguityProc thread to resultslist.");
				resultset.add(ambiworker);
				//ambithrd.start() called in coordAmbiguityProc constructor;
			
			}
			catch (Exception err){String msg = "exception"; }
			System.out.println("LingServer iiterates resultslist.");
			for (Future<coordAmbiguityResults> result : resultset){
				try{
					output += result.get().toJSON();
					System.out.println("LingServer resultslist item:" + output);
					}
				catch(InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
			os.println(output);	
			socket.close();
         }catch(SocketTimeoutException s)
         {
            System.out.println("Socket timed out!");
            break;
         }catch(IOException e)
         {
            e.printStackTrace();
            break;
         }
      }
   }



  // LingServer.main()  is procedural and must preload certain objects before it starts listening for requests for ambiguity detection.
  // LingServer.Main() preloads JWNL wordnet dictionaries and sets up WordNet's SimilarityMeasure Object 
  // SimilarityMeasure object must be referenced form LingServer.Sim
  // LingServer.Main() preloads a serialzed Classifier or creates one.
 
	// pass in the filename of a serialized TrainedClassifier with *.ser extension to deserialize a trainedClassifier
	// else build the classifier from a text file ending in *.txt;
 
  public static void main(String [] args)   throws InterruptedException  {
      int port = 1221;
      try{
    	  String fn = "";
		  // TODO: add commandline args to indicate stratup options:
		  //      Option 1 - desrilaize classier and training set; Option2 - create classifier with traingn set (classifier creator should serialize both Classifier and training set used to build Classifire.
		  //Current default behaviour, deserialize a Classifier 
		 boolean deserialize = true;
    	  if (args.length > 0 ){
    		  fn = args[0]; 
    		  if (fn.indexOf(".ser")>0 || fn.indexOf(".model")>0){deserialize = true;}
    		  //if (fn.indexOf(".txt")>0){deserialize = false;}
			}
    	  // Instance of a LingServer - declares an ambiclassifier{}, has a listen() method.
    	  LingServer ls = new LingServer(port);
    	  if (deserialize){ // deserialize a prebuilt classifier
    		  try  //TODO: filename hardcoded for now, refactor: TODO:serialized object files need consistent extension (".ser",".model"?) - refactor trainingclassifier...
    		  {  FileInputStream fin = new FileInputStream("../lib/logitClassifier_170obs.model");
    		     ObjectInputStream oin = new ObjectInputStream(fin);
				 //TODO: weka v3+ - refactor to use SerializationHelper()
    		     logitclassifier = (Classifier) oin.readObject(); //(Classifier) new LogitBoost(); // 
    		     oin.close();
    		     fin.close();
    		     System.out.println("LingServer.main() deserialized Classifier");
    		  }catch(IOException i){
    		     i.printStackTrace();
    		     return;
    		  }catch(ClassNotFoundException c){
    		     System.out.println("TrainingClassifier class not found");
    		     c.printStackTrace();
    		     return;
    		  }
    		  // deserialize the traingset data - required for aligning test data to data use to train classifier
			  // TODO: try-cathc - I know theres an exception for this (file not found?)
    		  trainingSet = (Instances) weka.core.SerializationHelper.read("../lib/traingdataset_170.ser");
	          System.out.println("LingServer.main() deserialized Classifier TraingSet");
    		  }
    	  else{  ambiclassifier.init();  } // build a new Classifier, takes several minutes min, (automatically serializes to disk after build)
               // load the wordnet dicts and create a similarity  measure object using ic-bnc-resnik.dat dictionaries..
               JWNL.initialize(new FileInputStream("C:\\projects\\node\\ling\\lib\\wordnet.xml"));
               //set up the parameters		
               Map<String,String> params = new HashMap<String,String>();
               System.out.println("params.put...");
               params.put("simType","shef.nlp.wordnet.similarity.JCn");
               params.put("infocontent","file:C:/projects/node/ling/lib/ic-bnc-resnik.dat");
               //create the similarity measure object
               System.out.println("create the similarity measure ");
               LSsim = SimilarityMeasure.newInstance(params);
               System.out.println("similarity measure preloaded");
               JWNLpreload = true;
               System.out.println("LingServer listening...");
               ls.listen();
			}
		catch(IOException ioe){ioe.printStackTrace();}
		catch(InterruptedException ie){ie.printStackTrace();}
		catch (Exception e) {e.printStackTrace();}
   }  // end main()
  
   
}