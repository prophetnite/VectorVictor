/*
   nm 2014-01-...
  TrainingClassifier, refactored from Tatiana's ClassifierTest class:
	- testData removed
	- class extends serializable.
	- when a logitBoost classifier is created and trained it is serialized. 
		- serailization obviates loadtime as the classifier takes along time to creat.
		- the LingServer looks for a serialzed Classifier.
	- when the classifier is serailized it's training set is also serialized:
		- the training set is preloaded by LingServer to be used to align tetsdata to traingdata.
	- the current traingset is hardwired: 170 observations in file named 'trainSet_obs170.txt"
	- the serialized traingset is written out to hardcoded 'trainigdataset_170.ser';
	- the serilazed classfire is written out to 'logiClassifier_170obs.model.
	
   
*/


import java.util.List;
import java.io.*;	
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import weka.classifiers.*;
import weka.classifiers.meta.LogitBoost;
import weka.core.*;

public class TrainingClassifier extends Thread implements java.io.Serializable
{
	//called by coordAmiguityProc to test sentence against classifier
	//TODO: make instance this.variables at instantiation (in createTestSet)?
	public static Classifier logit;
	public static Instances trainingSet;  // created on instantiation in main();
	
	public TrainingClassifier(){}

	//deprecated for weka.serailizationHelper methods inline below - always serialize the last classifier
	public static void serialize(Classifier c)	{
		   try
		      {
		         FileOutputStream fileOut = new FileOutputStream("trainingClassifier.ser");
		         ObjectOutputStream out = new ObjectOutputStream(fileOut);
		         out.writeObject(c);
		         out.close();
		         fileOut.close();
		         System.out.println("Serialized Classifier saved as trainingClassifier.ser");
		      }catch(IOException iox)
		      {
		          iox.printStackTrace();
		      }
		   }
	
	
	// coming from main run in the main thread
	public static void main(String[] args) throws Exception {
		System.out.println("TrainingClassifier.main()");
		TrainingClassifier tc = new TrainingClassifier();
		tc.init();
	}

	//calling externally, run in thread
	public void run() {	
		System.out.println(" Build Training set Started...");
		try {this.init();}
		catch (Exception e) { System.out.println("Build Training set Exception."); e.printStackTrace();}		
		System.out.println("Build Training set Ended.");
			}
	
	// initialze once
	public static void init()  throws Exception {
	
		List<CoordSent> trainData = new ArrayList<CoordSent>();
		//List<CoordSent> testData = new ArrayList<CoordSent>();
		/*
		 * The scan loop below expect a txt file in the form:
		 * modifier nearConjunct farConjunct modifierPOS nearConjunctPOS farConjunctPOS modifierLemma nearConjunctLemma farConjunctLemma pre||post nocuous||innocuous\n
		 * modifier nearConjunct ... etc.			
		 */
		//read in training data
		// 170 observations is the currently largest traing set we have. (10 patterns x ? )
		File trainF = new File("trainSet_obs170.txt");
		Scanner scan = new Scanner(trainF);
		while (scan.hasNextLine()){
			Scanner line = new Scanner(scan.nextLine());
			String mod = line.next();
			String nc = line.next();
			String fc = line.next();
			String mPOS = line.next();
			String ncPOS = line.next();
			String fcPOS = line.next();
			String mLemma = line.next();
			String ncLemma = line.next();
			String fcLemma = line.next();			
			boolean pre;
			if (line.next().equals("pre"))
				pre = true;
			else
				pre = false;
			String Atype = line.next();
			CoordSent sent = new CoordSent();
			sent.addInstance(sent.new CoordInstance(mod, nc, fc, mPOS, ncPOS, fcPOS, mLemma, ncLemma, fcLemma, pre, Atype));			
			trainData.add(sent);
		}

		//get heuristic data
		
		coordAmbiguityProc.sketchengine.login();
		coordAmbiguityProc.runHeuristics(trainData);
		
		//declare numeric attributes
		Attribute heur1 = new Attribute("distSim");
		Attribute heur2 = new Attribute("ColloFreq");
		Attribute heur3 = new Attribute("colloFreqLocal");
		Attribute heur4 = new Attribute("coordMatch");
		Attribute heur5 = new Attribute("morphoMatch");
		Attribute heur6 = new Attribute("semanticSim");
		
		//declare the class attribute
		FastVector classValue = new FastVector(2);
		classValue.addElement("nocuous");
		classValue.addElement("innocuous");
		Attribute classAt = new Attribute("ambiguity", classValue);
		
		//declare the feature vector
		FastVector attributes = new FastVector(7);
		attributes.addElement(heur1);
		attributes.addElement(heur2);
		attributes.addElement(heur3);
		attributes.addElement(heur4);
		attributes.addElement(heur5);
		attributes.addElement(heur6);
		attributes.addElement(classAt);
		
		//create an empty training set
		//declared public static at top
		trainingSet = new Instances("Ambiguity", attributes, trainData.size());
		trainingSet.setClassIndex(6);
		
		//populate train set
		for (int i = 0; i < trainData.size(); i++){
			//create the instances
			CoordSent.CoordInstance instData = trainData.get(i).getInstances().get(0);
			Instance trainInst = new Instance(7);
			trainInst.setValue((Attribute)attributes.elementAt(0), (double) instData.heurResults.get("distSim"));
			trainInst.setValue((Attribute)attributes.elementAt(1), (double) instData.heurResults.get("colloFreq"));
			trainInst.setValue((Attribute)attributes.elementAt(2), (double) instData.heurResults.get("colloFreqLocal"));
			trainInst.setValue((Attribute)attributes.elementAt(3), (int) instData.heurResults.get("coordMatch"));
			trainInst.setValue((Attribute)attributes.elementAt(4), (int) instData.heurResults.get("morphoMatch"));
			trainInst.setValue((Attribute)attributes.elementAt(5), (double) instData.heurResults.get("semanticSim"));
			trainInst.setValue((Attribute)attributes.elementAt(6), instData.Atype);
			//add the instance
			trainingSet.add(trainInst);
		}
		
		
		//create a logitboost classifier
		//Classifier logit = (Classifier)new LogitBoost();
		//made pubic, declared above.
		
		logit = (Classifier)new LogitBoost();
		logit.buildClassifier(trainingSet);
		//serailze traingset - required to set format of test data - see coordAmbiguityProc.formattestdata()
		weka.core.SerializationHelper.write("traingdataset_170.ser", trainingSet);
//		serialize(logit);
		weka.core.SerializationHelper.write("logitClassifier_170obs.model", logit);
		//test the classifier
		//Evaluation test = new Evaluation(trainingSet);
		//made public, declared above.
		//TCTest = new Evaluation(trainingSet);
		//test.evaluateModel(logit, testSet);
		
		System.out.println("Training Set Built");
						
	}
}
