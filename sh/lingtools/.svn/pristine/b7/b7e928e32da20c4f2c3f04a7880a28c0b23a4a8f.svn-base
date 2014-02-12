/*12/2/2013 Colin
 * 
 * 
 * usage: java coordAmbiguity <filename>
 * output: right now, the only outputs are the scores from the distributional similarity heuristic, and the total number of matched instances
 * notes:thinking about adding the heuristics to a different class
 * 		
 * 1/6/14 - heuristics return 0 if error is returned		
 */

//TODO: Decide what to do when a heuristic can't provide a result
//TODO:	Integrate WEKA LogitBoost algorithm
//TODO: Deal with British vs American spelling (when dealing with the British National Corpus)

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import shef.nlp.wordnet.similarity.SimilarityInfo;
import shef.nlp.wordnet.similarity.SimilarityMeasure;

import org.json.*;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.*; 
import edu.stanford.nlp.ling.CoreAnnotations.*;


public class coordAmbiguity {
	static int matchNum = 0;
	static String text = "";
		
	public static void main(String[] args) throws FileNotFoundException, JWNLException, Exception { 
		
		//if (args[0].endsWith(".txt")){
		//	File f = new File(args[0]);
		//	try {
		//		text = readFile(args[0], StandardCharsets.UTF_8);
		//	} catch (IOException e) {
		//		e.printStackTrace();
		//	}
		//}else{
		//	text = args[0];
			text = "The remaining space on the home page screen could be filled with introductory text and hypertext links to other parts of the web site";
		//}		

		Properties props = new Properties(); 
		props.put("annotators", "tokenize, ssplit, pos, lemma");	//other annotator options at: http://nlp.stanford.edu/software/corenlp.shtml		

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);		
		Annotation document = pipeline.process(text);

		List<CoordSent> andSents = (LinkedList<CoordSent>) grabAndSents(document); 
		System.out.print("Matched 'ands' total: " + matchNum);
		buildRoughSurvey(andSents);
		//runHeuristics(andSents);
		int bloo = 0;
		
//		for (CoordSent sentence: andSents){
//			for (CoordSent.CoordInstance instance: sentence.getInstances()){
//				System.out.println("modifier: " + instance.modifier + "; near conjunct: " + instance.nearConjunct + "; far conjunct: " + instance.farConjunct);
//				Set<String> keys = instance.heurResults.keySet();
//				for (String key: keys){
//					System.out.print(key + ": " + instance.heurResults.get(key) + "; ");
//				}
//				System.out.println();
//			}			
//		}
	}


	/*
	 * Finds sentences containing "and" and sends their indexes(in the List<String>) off to patternMatch.
	 * Returns the list of matched sentences.
	 */
	public static List<CoordSent> grabAndSents(Annotation document) {
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
		return allSents;
	}


	/* 	  
	 * Matches the POS tags in the instances of "and" against the predefined patterns in POSPatternSet.
	 * 
	 */
	public static void patternMatch(CoordSent sentence){
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

	
	public static void runHeuristics(List<CoordSent> sentences) throws Exception{
		//get local WordNet data from this file
		JWNL.initialize(new FileInputStream("C:\\projects\\node\\ling\\lib\\wordnet.xml"));
		//set up the parameters		
		Map<String,String> params = new HashMap<String,String>();
		params.put("simType","shef.nlp.wordnet.similarity.JCn");
		params.put("infocontent","file:C:/projects/node/ling/lib/ic-bnc-resnik.dat");
		//create the similarity measure object
		SimilarityMeasure sim = SimilarityMeasure.newInstance(params);
		
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
				distFarQuery.put("lpos", posConvert(instance.getSentencePOS()[instance.farConjunctIndex]));
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
				coordFarQuery.put("lpos", posConvert(instance.getSentencePOS()[instance.farConjunctIndex]));
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
		// SketchEngineQuery collxSketch = new SketchEngineQuery("collx", collxAttrs, colloFreqQueries); 
		SketchEngineQueryObj collxSketch = new SketchEngineQueryObj();
		List<JSONObject> colloFreqList = null;
		if (collxSketch.login()) {colloFreqList = collxSketch.runQuery(	"collx", collxAttrs, colloFreqQueries); }
		//List<JSONObject> colloFreqList = collxSketch.getResults();
		
		//run distsim, get results
		Map<String,String> thesAttrs = new HashMap<String, String>();
		thesAttrs.put("corpname", "preloaded/bnc2");
		thesAttrs.put("maxthesitems", "200");
		thesAttrs.put("format", "json");
//		SketchEngineQuery thesSketch = new SketchEngineQuery("thes", thesAttrs, distSimQueries); 
		SketchEngineQueryObj thesSketch = new SketchEngineQueryObj();
		List<JSONObject> distSimList = null;
		if (thesSketch.login()) {distSimList = collxSketch.runQuery("thes", thesAttrs, distSimQueries); }
		//List<JSONObject> distSimList = thesSketch.getResults();
			
		//run coordmatch, get results
		Map<String,String> wsketchAttrs = new HashMap<String, String>();
		wsketchAttrs.put("corpname", "preloaded/bnc2");
		wsketchAttrs.put("minfreq", "1");
		wsketchAttrs.put("format", "json");
//		SketchEngineQuery wsketchSketch = new SketchEngineQuery("wsketch", wsketchAttrs, coordMatchQueries); 
		SketchEngineQueryObj wsketchSketch  = new SketchEngineQueryObj();
		List<JSONObject> coordMatchList = null;
		if (wsketchSketch.login()) {coordMatchList = wsketchSketch.runQuery("wsketch", wsketchAttrs, coordMatchQueries); }
//		List<JSONObject> coordMatchList = wsketchSketch.getResults();
		
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
		String nearConjunct = instance.nearConjunct;
		String farConjunct = instance.farConjunct;
		double[] score = new double[2];
		double finalScore;
		boolean error = false;
		
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
				error = true;
			}			
		}
		//keep the high score
		if (error)
			finalScore = 0.0;
		else
			finalScore = Math.max(score[0], score[1]);
		instance.heurResults.put("distSim", finalScore);
		//System.out.println("NC: " + nearConjunct + ", " + "FC: " + farConjunct + ": " + finalScore);		
	}	
	
	/*
	* Gets the ratio of (nearConjunct + modifier collocation frequency)/(farConjunct + modifier collocation frequency) in local document
	* 
	*/
	public static void colloFreq(CoordSent.CoordInstance instance, String text){
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
	    //System.out.print("NC:"+instance.nearConjunct+"; FC:"+instance.farConjunct+"; M:"+instance.modifier+"; counts:"+count[0]+count[1]);
	}
	
	
	/*attrs.put("q", "q" + query_list[i]);
	 * Gets the ratio of (nearConjunct + modifier collocation frequency)/(farConjunct + modifier collocation frequency) in corpus
	 * thinking about adding the heuristics to a different class 
	 */
	public static void colloFreq(CoordSent.CoordInstance instance, JSONObject data){
		String nearConjunct = instance.nearConjunct;
		String farConjunct = instance.farConjunct;
		double[] scores = new double[2];
		boolean error = false;
		
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
				error = true;
			}			
		}
		//no zero denominators allowed! sorry zeros
		if (scores[1] == 0 || error){
			scores[1] = .01;//should be smaller than this?
		}
		double finalScore = scores[0]/scores[1];
		instance.heurResults.put("colloFreq", finalScore);
		//System.out.println("NC/FC: " + finalScore);
	}	


	public static void coordMatch(CoordSent.CoordInstance instance, JSONObject[] data){
		String nearConjunct = instance.nearConjunct;
		String farConjunct = instance.farConjunct;
		int[] score = new int[2];
		int finalScore;
		boolean error = false;
		
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
				error = true;
			}
		}
		if (!error)
			finalScore = Math.max(score[0], score[1]);
		else
			finalScore = 0;
		instance.heurResults.put("coordMatch", finalScore);
		//System.out.println("NC: " + nearConjunct + ", " + "FC: " + farConjunct + ": " + finalScore);			
	}
	
	
	//SHOULD RETURN AN INT
	public static void morphoMatch(CoordSent.CoordInstance instance){
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
	        //System.out.println("NC: " + nearConjunct + ", FC: " + farConjunct + ":" + count);
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
	
	//build a csv
	public static void buildRoughSurvey(List<CoordSent> andSents) throws IOException {
		FileWriter writer = new FileWriter("fix.csv");
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
				writer.append(andSents.get(i).getSentence() + delimiter);				
				//write instance;
				writer.append(instance.getInstance() + delimiter);				
				//write modifier;
				writer.append(instance.modifier + delimiter);				
				//write nc;
				writer.append(instance.nearConjunct + delimiter);				
				//write fc;
				writer.append(instance.farConjunct + delimiter);				
				//write pos and lemmas				
				writer.append(instance.getSentencePOS()[instance.modifierIndex] + delimiter);				
				writer.append(instance.getSentencePOS()[instance.nearConjunctIndex] + delimiter);				
				writer.append(instance.getSentencePOS()[instance.farConjunctIndex] + delimiter);
				writer.append(instance.getSentenceLemmas()[instance.modifierIndex] + delimiter);
				writer.append(instance.getSentenceLemmas()[instance.nearConjunctIndex] + delimiter);
				writer.append(instance.getSentenceLemmas()[instance.farConjunctIndex] + delimiter);
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