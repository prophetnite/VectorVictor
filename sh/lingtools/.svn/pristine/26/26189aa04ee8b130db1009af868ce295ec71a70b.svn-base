/*

 Colin's coordAmbiguity renamed coordAmbiguityProc.
 Code refactored:
	- to implement Callable() (JAVA threading inteface that can pass out values on completion).
	- procedural method parsed into functions callable by LingServer.

12/2/2013 Colin
 * 
 * 
 * usage: java coordAmbiguity <filename>
 * output: right now, the only outputs are the scores from the distributional similarity heuristic, and the total number of matched instances
 * notes:thinking about adding the heuristics to a different class
 * 		
 * 		
 */

//TODO: Decide what to do when a heuristic can't provide a result
//TODO:	Integrate WEKA LogitBoost algorithm
//TODO: Deal with British vs American spelling (when dealing with the British National Corpus)


// threading libs
import java.net.*;
import java.lang.Thread;
import java.lang.SecurityException;
import java.util.concurrent.Callable;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import org.json.*;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import shef.nlp.wordnet.similarity.SimilarityInfo;
import shef.nlp.wordnet.similarity.SimilarityMeasure;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.*; 
import edu.stanford.nlp.ling.CoreAnnotations.*;
import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.meta.LogitBoost;

public class coordAmbiguityProc implements Callable<coordAmbiguityResults>  {
	
	static int matchNum = 0;  // deprecate for this callable instance
	static String text = "";  // passed in by LingSrerver for anlysis
	public static long startTime = System.currentTimeMillis();  // timers
	public static long stopTime = System.currentTimeMillis();	// timers

	// 1-time preinit the sketchengine object
	// TODO: login, get an HTTP client/session; keep open...???
	static SketchEngineQueryObj sketchengine = new SketchEngineQueryObj(); 

	// no-json data delims -  UTF8 safe and efficient
		// deprecated - using coordAmbiguityResults class to pass back results
	public static final String STX = Character.toString ((char) 2); //start of text
	public static final String ETX = Character.toString ((char) 3); //end of text
	public static final String GS = Character.toString ((char) 29); //group seperatorpublic static final 
	public static final String RS = Character.toString ((char) 30); //record seperator
    public static final String US = Character.toString ((char) 31); //unit seperator

	public coordAmbiguityResults results = null;
       	
	public coordAmbiguityProc() { };
	
	public coordAmbiguityProc(String tname, String anotxt) {
		System.out.println("coordAmbiguityProc constructor: " + Thread.currentThread().getName() + ", thrdname: " + tname + " -  processing " + this.text);
		this.text = anotxt;
		//TODO: instantiate sketchengine here?
	}
	
	
	// call() is executed when super is submitted as a thread from the LingServer, to a threadpool, when it gets a text chunk from node to process
	// in coordAmbiguityProc call() is a thread that processes single sentences
	public coordAmbiguityResults call() throws Exception { 
		System.out.println("coordAmbiguityProc.call().");
		//text = "The home page screen could be filled with introductory text and hypertext links to other parts of the web site."; //ands=2
	
		sketchengine.login();	// sets sketchengine.client and authenticates 1 time.
								// TODO: persist the HTTP connection thru all the queries?
								// TODO: persist tye HTTP connection and client in LingSever, ie leave conection open;  set connection timeout; yest if connected every 5 minits?

		//clear results set each thread instantiation
		results = new coordAmbiguityResults();	// the reulst object has a toJSON() method, is returned from the coordAmbiguityProc instance to LingServer. LingServer routes it c back to nodejs who HTMLsend it back to user-agent (website).
		Properties props = new Properties(); 
		props.put("annotators", "tokenize, ssplit, pos, lemma");	//other annotator options at: http://nlp.stanford.edu/software/corenlp.shtml		
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);		
		Annotation document = pipeline.process(this.text);
		List<CoordSent> andSents = null;
		List<Instance> testset = null;

		try {
            // 1. test for CCs (and | or); then 2. inner call to test for pattern matches
			andSents = (LinkedList<CoordSent>) grabAndSents(document); 
			System.out.println("grabAndSents -  Ands:" + results.ccCount +", Matched Patterns:" + results.patternCount);
			if (results.ccCount == 0) {return results;}
            
            // if we detect a target sentence (contains and|or and a pattern match) then add array of sentence tokens to return object.
            // the tokens list will be used to calculate where to hilite risky modifier-conjunction patterns in the sentence client-side.
			List<CoreLabel> tokens = document.get(SentencesAnnotation.class).get(0).get(TokensAnnotation.class);
		    for (CoreMap token: tokens){ this.results.tokens.add(token.get(TextAnnotation.class).toString()); }
		
			// runheuristics on AndSentences
			runHeuristics(andSents);
			// create a testset for weka evaluation against trained dataset
			// the test set is derived from the andSents instances and is aligned record by record
			testset = formatTestData(andSents);
			double[] fDistribution;  			//[0] = nocuous, [1] = innocuous
			int c = 0;
			coordAmbiguityResults.observation obs = null; 
			// run the classifier, aggregate classifier results and at the same time other attributes of the snetence. this is the 'computational object' for the user-agent.
			for (Instance testdata: testset){
				System.out.println("coordAmbiguityProc - LingServer.logitclassifier.distributionForInstance()");
				fDistribution = LingServer.logitclassifier.distributionForInstance(testdata);
				CoordSent.CoordInstance sentenceInst = andSents.get(0).getInstances().get(c); 
				obs = new coordAmbiguityResults().new observation(
							fDistribution[0],
							fDistribution[1],
							sentenceInst.modifier,
							sentenceInst.nearConjunct,
							sentenceInst.farConjunct,
							sentenceInst.modifierIndex,
							sentenceInst.nearConjunctIndex,
							sentenceInst.farConjunctIndex
							);
				this.results.observations.add(obs);
				c++;  // get next instance from sentence object
			}
			
			}
		catch(Exception ex){ex.printStackTrace();	}
		return results;
	}


	/*
	 * Finds sentences containing "and" and sends their indexes(in the List<String>) off to patternMatch.
	 * Returns the list of matched sentences.
	 */
	//public static List<CoordSent> grabAndSents(Annotation document) {
	public List<CoordSent> grabAndSents(Annotation document) {
		System.out.println("coordAmbiguityProc.grabAndSents().\n");
		List<CoordSent> allSents = new LinkedList<CoordSent>();
		int andCount = 0;
		int originalMatch = 0;
		Pattern pattern = Pattern.compile(" and | or ");
				
		for(CoreMap sentence: document.get(SentencesAnnotation.class)) { //for each sentence in document	
			List<Integer> indexes = new ArrayList<Integer>();
			Matcher matcher = pattern.matcher(sentence.get(TextAnnotation.class));
			
			if (matcher.find()){
				for (int i = 0; i < sentence.get(TokensAnnotation.class).size(); i++) { //for each token in sentence
					CoreLabel token = sentence.get(TokensAnnotation.class).get(i);    
					if (token.get(TextAnnotation.class).matches("and|or")) {
						indexes.add(i);	
						andCount++;
					}								
				}
			}			
			
			//create CoordSent object and assign indexes to it
			CoordSent sent = new CoordSent(sentence, indexes);
			patternMatch(sent);
			if (sent.sentMatch){
				originalMatch++;
				allSents.add(sent);
			}
		}
		//System.out.println("# of ands: " + andCount);
		//System.out.println("# of matches before processing: " + originalMatch);
		this.results.ccCount = andCount;
		this.results.patternCount = originalMatch;
		return allSents;
	}


	/* 	  
	 * Matches the POS tags in the instances of "and" against the predefined patterns in POSPatternSet.
	 * 
	 */
	public static void patternMatch(CoordSent sentence){
		System.out.println("coordAmbiguity.patternMatch().");

		POSPatternSet patternSet = new POSPatternSet();		
		
		//run the matching and labeling for each "and" in the sentence
		for(int j = 0; j < sentence.getAndIndexes().size(); j++){ 
			int andIndex = sentence.getAndIndexes().get(j);
			
			//run the matching for one patternType at a time
			for (int i = 0 ; i < patternSet.patternList.length; i++ ){
				PatternType type = patternSet.patternList[i];

				//run the matching for one pattern at a time
				for(int k = 0; k < patternSet.patternList[i].patterns.length; k++){
					String[] pattern = type.patterns[k];
					int patternSize = pattern.length;
					int patternAndIndex = patternSet.patternList[i].andIndex;						

					//choose the correct ngram to match against the pattern
					int beginRangeIndex = andIndex - patternAndIndex;
					int endRangeIndex = beginRangeIndex + patternSize; //exclusive

					//breaks loop if the subString leads to IndexOutOfBounds
					if (beginRangeIndex < 0 || endRangeIndex > sentence.size()){break;} 

					String[] subAndSent = Arrays.copyOfRange(sentence.getSentencePOS(), beginRangeIndex, endRangeIndex);

					boolean match = false;
					//this loop matches the POS strings in the pattern to those in the instance
					for (int m = 0; m < patternSize; m++){
						match = subAndSent[m].matches(pattern[m]);
						if (!match)
							break;						
					}					

					if (match){ 
						matchNum++;
						sentence.sentMatch = true;						
						
						//save appropriate data to the CoordInstance
						boolean pre = (type.labels.get("M") < type.labels.get("NC"));
						int[] bounds = new int[]{beginRangeIndex, endRangeIndex};
						int ncIndex = beginRangeIndex + type.labels.get("NC");
						int fcIndex = beginRangeIndex + type.labels.get("FC");
						int modIndex = beginRangeIndex + type.labels.get("M");
						String nc = sentence.getSentenceTokens()[ncIndex];
						String fc = sentence.getSentenceTokens()[fcIndex];
						String mod = sentence.getSentenceTokens()[modIndex];
						sentence.addInstance(sentence.new CoordInstance(pre, bounds, ncIndex, fcIndex, modIndex, nc, fc, mod, pattern));						
					}					
				}
			}
		}
	}

	public static void runHeuristics(List<CoordSent> sentences) throws Exception {
		System.out.println("coordAmbiguity.runHeuristics().");
		//TODO: this only works if the LingServer is running...!
		// preloaded in LingServer?...
		if (!LingServer.JWNLpreload){
			//get local WordNet data from this file
			System.out.println("JWNL.initialize");
			JWNL.initialize(new FileInputStream("C:\\projects\\node\\ling\\lib\\wordnet.xml"));
			//set up the parameters		
			Map<String,String> params = new HashMap<String,String>();
			System.out.println("params.put...");
			params.put("simType","shef.nlp.wordnet.similarity.JCn");
			params.put("infocontent","file:C:/projects/node/ling/lib/ic-bnc-resnik.dat");
			//create the similarity measure object
			System.out.println("create the similarity measure ");
			SimilarityMeasure sim = SimilarityMeasure.newInstance(params);
		}
		SimilarityMeasure sim = LingServer.LSsim;
		List<Map<String,String>> distSimQueries  = new ArrayList<Map<String,String>>();
		List<Map<String,String>> colloFreqQueries  = new ArrayList<Map<String,String>>();
		List<Map<String,String>> coordMatchQueries  = new ArrayList<Map<String,String>>();
		
		for (CoordSent sentence: sentences){
			for (CoordSent.CoordInstance instance: sentence.getInstances()){
				//add instance to distSim querylist
				Map<String, String> distNearQuery = new HashMap<String, String>();
				distNearQuery.put("lemma", instance.getSentenceLemmas()[instance.nearConjunctIndex]);
				distNearQuery.put("lpos", posConvert(instance.getSentencePOS()[instance.nearConjunctIndex]));
				distSimQueries.add(distNearQuery);
				Map<String, String> distFarQuery = new HashMap<String, String>();
				distFarQuery.put("lemma", instance.getSentenceLemmas()[instance.farConjunctIndex]);
				distFarQuery.put("lpos", posConvert(instance.getSentenceLemmas()[instance.farConjunctIndex]));
				distSimQueries.add(distFarQuery);
				
				//add instance to colloFreq querylist
				Map<String, String> query = new HashMap<String, String>();
				query.put("q", "q[lemma=\"" + instance.getSentenceLemmas()[instance.modifierIndex] + "\"]");
				colloFreqQueries.add(query);
				
				//add instance to coordMatch querylist
				Map<String, String> coordNearQuery = new HashMap<String, String>();
				coordNearQuery.put("lemma", instance.getSentenceLemmas()[instance.nearConjunctIndex]);
				coordNearQuery.put("lpos", posConvert(instance.getSentencePOS()[instance.nearConjunctIndex]));
				coordMatchQueries.add(coordNearQuery);
				Map<String, String> coordFarQuery = new HashMap<String, String>();
				coordFarQuery.put("lemma", instance.getSentenceLemmas()[instance.farConjunctIndex]);
				coordFarQuery.put("lpos", posConvert(instance.getSentenceLemmas()[instance.farConjunctIndex]));
				coordMatchQueries.add(coordFarQuery);
				
				//get morpho score
				morphoMatch(instance);
				
				//get colloFreqLocal score
				colloFreq(instance,text);
				
				//get semanticSim score
				double simResult;
				if (sim.getSimilarity(instance.nearConjunct, instance.farConjunct) != null){
					SimilarityInfo simInfo = sim.getSimilarity(instance.nearConjunct,instance.farConjunct);
					simResult = simInfo.getSimilarity();
				}
				else
					simResult = 0;
					instance.heurResults.put("semanticSim", simResult);
				}
			}
		
		//run collofreq, get results
		Map<String,String> collxAttrs = new HashMap<String, String>();
		collxAttrs.put("corpname", "preloaded/bnc2");
		collxAttrs.put("format", "json");
		//. login here - remove login() call from TrainigClassifier.init() line 60.
		List<JSONObject> colloFreqList = sketchengine.runQuery("collx", collxAttrs, colloFreqQueries);
		
		
		//run distsim, get results
		Map<String,String> thesAttrs = new HashMap<String, String>();
		thesAttrs.put("corpname", "preloaded/bnc2");
		thesAttrs.put("maxthesitems", "200");
		thesAttrs.put("format", "json");
		List<JSONObject> distSimList = sketchengine.runQuery("thes", thesAttrs, distSimQueries);
			
		//run coordmatch, get results
		Map<String,String> wsketchAttrs = new HashMap<String, String>();
		wsketchAttrs.put("corpname", "preloaded/bnc2");
		wsketchAttrs.put("minfreq", "1");
		wsketchAttrs.put("format", "json");

		List<JSONObject> coordMatchList = sketchengine.runQuery("wsketch", wsketchAttrs, coordMatchQueries);
		
		//calculate scores
		Iterator<JSONObject> distSimJSONiter = distSimList.iterator();
		Iterator<JSONObject> colloFreqJSONiter = colloFreqList.iterator();
		Iterator<JSONObject> coordMatchJSONiter = coordMatchList.iterator();
		for (int i = 0; i < sentences.size(); i++){
			for (int k = 0; k < sentences.get(i).getInstances().size(); k++){
				
				//match distSim result to instance
				JSONObject[] distData = new JSONObject[]{distSimJSONiter.next(), distSimJSONiter.next()};
				distSim(sentences.get(i).getInstances().get(k), distData);
				
				//match colloFreq result to instance
				JSONObject colloData = colloFreqJSONiter.next();
				colloFreq(sentences.get(i).getInstances().get(k), colloData);
				
				//match coordmatch result to instance
				JSONObject[] coordData = new JSONObject[]{coordMatchJSONiter.next(), coordMatchJSONiter.next()};
				coordMatch(sentences.get(i).getInstances().get(k), coordData);
			}
		}		
	}
	
	
//************Heuristics*******************//
	
	
	/*
	 * Gets contextual similarity scores between the near and far conjuncts
	 * 
	 */
	public static void distSim(CoordSent.CoordInstance instance, JSONObject[] data){
		System.out.println("coordAmbiguity.distSim().");

		String nearConjunct = instance.nearConjunct;
		String farConjunct = instance.farConjunct;
		double[] score = new double[2];

		//compare the scores
		for (int i = 0; i < 2 ; i++){
			String secondaryConjunct;
			if ( i == 0){
				secondaryConjunct = farConjunct;
			}else {
				secondaryConjunct = nearConjunct;
			}					
			try{				
				JSONArray words = (JSONArray) data[i].get("Words");		

				for (int k = 0; k < words.length(); k++){ 
					JSONObject res = (JSONObject)words.get(k);
					//System.out.println(res.get("word") + "\t" + res.getDouble("score"));
					if (res.getString("word").equals(secondaryConjunct)){

						score[i] = res.getDouble("score");
					}
				}
			}catch(JSONException e){
				//System.out.print(job.get("error") + "  ||  ");				
			}			
		}
		//keep the high score
		double finalScore = Math.max(score[0], score[1]);
		instance.heurResults.put("distSim", finalScore);
		System.out.println("distSim - NC: " + nearConjunct + ", " + "FC: " + farConjunct + ": " + finalScore);		
	}	
	
	/*
	* Gets the ratio of (nearConjunct + modifier collocation frequency)/(farConjunct + modifier collocation frequency) in local document
	* 
	*/
	public static void colloFreq(CoordSent.CoordInstance instance, String text){
		System.out.println("coordAmbiguity.colloFreq(). 1");
		String[] conjuncts = new String[]{instance.nearConjunct, instance.farConjunct};
		double[] count = new double[]{0,0};

	    //search loop
	    for(int i = 0; i < 2; i++){
	        Pattern searchPattern;
	        if (instance.pre) {
	            searchPattern = Pattern.compile(instance.modifier + " ([a-zA-Z]* ){0,3}" + conjuncts[i]);                                               
	        }else{
	            searchPattern = Pattern.compile(conjuncts[i] + " ([a-zA-Z]* ){0,3}" + instance.modifier);                                               
	        }
	        Matcher matcher = searchPattern.matcher(text);
	        while (matcher.find()){
	        count[i]++;
	        }
	    }
	    if (count[1] == 0){
	    	count[1] = .01;
	    }
	    double finalScore = count[0]/count[1];
	    instance.heurResults.put("colloFreqLocal", finalScore);
	    System.out.print("colloFreq1 - NC:"+instance.nearConjunct+"; FC:"+instance.farConjunct+"; M:"+instance.modifier+"; counts:"+count[0]+count[1]);
	}
	
	
	/*attrs.put("q", "q" + query_list[i]);
	 * Gets the ratio of (nearConjunct + modifier collocation frequency)/(farConjunct + modifier collocation frequency) in corpus
	 * thinking about adding the heuristics to a different class 
	 */
	public static void colloFreq(CoordSent.CoordInstance instance, JSONObject data){
		System.out.println("coordAmbiguity.colloFreq(). 2");
		String nearConjunct = instance.nearConjunct;
		String farConjunct = instance.farConjunct;
		double[] scores = new double[2];
		
		//compare the scores
		for (int i = 0; i < 2 ; i++){
			String conjunct;
			if ( i == 0){
				conjunct = nearConjunct;
			}else {
				conjunct = farConjunct;
			}					
			try{
				JSONArray items = (JSONArray)data.get("Items");		

				for (int k = 0; k < items.length(); k++){
					JSONObject result = (JSONObject)items.get(k);

					if (result.getString("str").equals(conjunct)){
						//System.out.println(result.getString("str") + ": " + result.getDouble("freq"));
						scores[i] = result.getDouble("freq");
						break;
					}
				}
			}catch(JSONException e){
				//System.out.print(job.get("error") + "  ||  ");					
			}			
		}
		if (scores[1] == 0){
			scores[1] = .01;//should be smaller than this
		}
		double finalScore = scores[0]/scores[1]; //have to make sure no zero denominator before this
		instance.heurResults.put("colloFreq", finalScore);
		System.out.println("colloFreq2 - NC/FC: " + finalScore);
	}	


	public static void coordMatch(CoordSent.CoordInstance instance, JSONObject[] data){
		System.out.println("coordAmbiguity.coordMatch().");
	
		String nearConjunct = instance.nearConjunct;
		String farConjunct = instance.farConjunct;
		int[] score = new int[2];
		
		//compare the scores and keep the high score
		for (int i = 0; i < 2 ; i++){
			String secondaryConjunct;
			if ( i == 0){
				secondaryConjunct = farConjunct;
			}else {
				secondaryConjunct = nearConjunct;
			}					
			try{
				JSONArray gramRels = (JSONArray)data[i].get("Gramrels");	
				JSONObject words = (JSONObject)gramRels.get(0);
				JSONArray items = (JSONArray)words.get("Words");
				for (int k = 0; k < items.length(); k++){
					JSONObject res = (JSONObject)items.get(k); 
					if (res.getString("word").equals(secondaryConjunct)){
						score[i] = k+1;
					}
				}
			}catch(JSONException e){
				//System.out.print(job.get("error") + "  ||  ");					
			}
		}
		int finalScore = Math.max(score[0], score[1]);
		instance.heurResults.put("coordMatch", finalScore);
		System.out.println("coordMatch - NC: " + nearConjunct + ", " + "FC: " + farConjunct + ": " + finalScore);			
	}
	
	
	//SHOULD RETURN AN INT
	public static void morphoMatch(CoordSent.CoordInstance instance){
		System.out.println("coordAmbiguity.morphoMatch().");

		String nearConjunct = instance.nearConjunct;
		        String farConjunct = instance.farConjunct;
		        
		        //compare up to 6 trailing characters
		        int conjunctMin = Math.min(nearConjunct.length(), farConjunct.length());
		        int count = 0;
		        
		        // think about breaking if there is no match!!
		        for (int i = 1; i <= Math.min(conjunctMin, 6); i++){
		                if(nearConjunct.charAt(nearConjunct.length() - i) == farConjunct.charAt(farConjunct.length() - i)){
		                        count++;
		                }
		        }
		        instance.heurResults.put("morphoMatch", count);
		        System.out.println("morphoMatch - NC: " + nearConjunct + ", FC: " + farConjunct + ":" + count);
	}
		
	
	/*
	 * Reads in document to be processed.
	 * 
	 */
	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	
	/*
	 * SketchEngine queries boil down the intricate pos tags available into three categories: noun, verb, adjective
	 * this method converts the tagged words into the format SketchEngine expects
	 * 
	 */	
	public static String posConvert(String pos){
		if (pos.matches("N.+")){
			return "-n";
		}else if (pos.matches("V.+")){
			return "-v";
		}else if(pos.matches("J.+")){
			return "-j";
		}else
			return "posConvert error";		
	}

	public List<Instance> formatTestData(List<CoordSent> andSents){
		System.out.println("coordAmbiguity.formatTestData().");
		
		List<Instance> instances = new ArrayList<Instance>();
		//declare numeric attributes
		Attribute heur1 = new Attribute("distSim");
		Attribute heur2 = new Attribute("ColloFreq");
	//	Attribute heur3 = new Attribute("colloFreqLocal");
		Attribute heur4 = new Attribute("coordMatch");
		Attribute heur5 = new Attribute("morphoMatch");
		Attribute heur6 = new Attribute("semanticSim");

		//declare the feature vector
		FastVector attributes = new FastVector(6);
		attributes.addElement(heur1);
		attributes.addElement(heur2);
	//	attributes.addElement(heur3);
		attributes.addElement(heur4);
		attributes.addElement(heur5);
		attributes.addElement(heur6);
		
		Instance testInst = null; // = new Instance(6);
		Instances testSet = new Instances("Ambiguity", attributes, andSents.size());
		testSet.setClassIndex(4);
		//TODO: we really want this demo for 1 senetnce at a time only.  1 sentnce may have more than 1 instance if multi and/or or patterns.
		// this should always be 1 sentence/text-chunk and andsents.size should = 1.
		// but in the case we need to start accepting multople sentences... top 'for loop'.
		for (int i = 0; i < andSents.size(); i++){
			List<CoordSent.CoordInstance> andinstances = andSents.get(i).getInstances();  // enumerable
			for (CoordSent.CoordInstance instData: andinstances) {
				try{
				testInst = new Instance(6);
/*				testInst.setValue((Attribute)heur1, (double) instData.heurResults.get("distSim"));
				testInst.setValue((Attribute)heur2, (double) instData.heurResults.get("colloFreq"));
				testInst.setValue((Attribute)heur4, (int) instData.heurResults.get("coordMatch"));
				testInst.setValue((Attribute)heur5, (int) instData.heurResults.get("morphoMatch"));
				testInst.setValue((Attribute)heur6, (double) instData.heurResults.get("semanticSim"));
				//leave index 6 empty
		*/
				testInst.setValue((Attribute)attributes.elementAt(0), (double) instData.heurResults.get("distSim"));
				testInst.setValue((Attribute)attributes.elementAt(1), (double) instData.heurResults.get("colloFreq"));
				testInst.setValue((Attribute)attributes.elementAt(2), (int) instData.heurResults.get("coordMatch"));
				testInst.setValue((Attribute)attributes.elementAt(3), (int) instData.heurResults.get("morphoMatch"));
				testInst.setValue((Attribute)attributes.elementAt(4), (double) instData.heurResults.get("semanticSim"));
				//leave index 6 empty
				}
				catch (Exception ex){ex.printStackTrace();}
				testInst.setDataset(LingServer.trainingSet);				
				instances.add(testInst);
				}
			}
		return instances;	
	}

	
	//build a csv
	public static void buildRoughSurvey(List<CoordSent> andSents) throws IOException {
		FileWriter writer = new FileWriter("matchItems.csv");
		CharSequence delimiter = "~";
		for (int i = 0; i < andSents.size(); i++){ //for every sentence
			List<CoordSent.CoordInstance> instanceList = andSents.get(i).getInstances();

			for (CoordSent.CoordInstance instance: instanceList) { //for every instance
				//write pattern type;
				for (int k = 0; k < instance.pattern.length; k++){
					writer.append(instance.pattern[k] + " ");
				}
				writer.append(delimiter);
				//write sentence
				writer.append(andSents.get(i).getSentence());
				writer.append(delimiter);
				//write instance;
				writer.append(instance.getInstance());
				writer.append(delimiter);
				//write modifier;
				writer.append(instance.modifier);
				writer.append(delimiter);
				//write nc;
				writer.append(instance.nearConjunct);
				writer.append(delimiter);
				//write fc;
				writer.append(instance.farConjunct);
				writer.append(delimiter);
				//write pre or post;
				if (instance.pre)
					writer.append("pre");
				else
					writer.append("post");
				writer.append(delimiter);
				//write high;				
				//write low;
				writer.append('\n');
			}
			
		}
		writer.close();
	}
	
}