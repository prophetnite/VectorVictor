/**
 * Storage object for the sentence and instance data
   TODO: nm coordsent contains coordInstance, remove seperate coordInstance.java files
 
 */
import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class CoordSent {   
	boolean sentMatch = false; 
	private String sentence;
	private String[] sentenceTokens;
	protected String[] sentencePOS;
	private	String[] sentenceLemmas;
	private List<Integer> andIndexes;
	private List<CoordInstance> instances = new ArrayList<CoordInstance>();
		
	public CoordSent(){}
	public CoordSent(CoreMap sentence, List<Integer> andIndexes){
		this.andIndexes = andIndexes;
		this.sentenceTokens = new String[sentence.get(TokensAnnotation.class).size()];
		this.sentencePOS = new String[sentence.get(TokensAnnotation.class).size()];
		this.sentenceLemmas = new String[sentence.get(TokensAnnotation.class).size()];
		this.sentence = sentence.get(TextAnnotation.class).replaceAll("\n|\r", " ");
		
		for (int i = 0; i < sentence.get(TokensAnnotation.class).size(); i++){
			sentenceTokens[i] = sentence.get(TokensAnnotation.class).get(i).get(TextAnnotation.class);
			sentencePOS[i] = sentence.get(TokensAnnotation.class).get(i).get(PartOfSpeechAnnotation.class);
			sentenceLemmas[i] = sentence.get(TokensAnnotation.class).get(i).get(LemmaAnnotation.class);
		}
	}
	
	public int size(){return sentenceTokens.length;}
	public List<Integer> getAndIndexes(){return andIndexes;}
	public List<CoordInstance> getInstances(){return instances;}
	public String[] getSentenceTokens(){return sentenceTokens;}
	public String[] getSentencePOS(){return sentencePOS;}
	public String[] getSentenceLemmas(){return sentenceLemmas;}
	public void removeInstance(int index){instances.remove(index);}
	public void addInstance(){instances.add(new CoordInstance());}
	public void addInstance(CoordInstance instance){instances.add(instance);}
	public String getSentence(){return sentence;}
	
	
	/*
	 * CoordInstance objects hold info about a single matched instance
	 */
	public class CoordInstance {
		String Atype;
		
		boolean pre = false; //pre or post modification
		//boolean instanceMatch = false;	//indicates whether this coordination instance matches one of the patterns
		int[] boundaryIndexes = new int[2]; //the indexes of the first(inclusive) and last(exclusive) members of the instance in the original sentence
		
		//indexes refer to original sentence, NOT the instance		
		int modifierIndex;
		int nearConjunctIndex;
		int farConjunctIndex;
		
		String[] pattern;
		String modifier;
		String nearConjunct;
		String farConjunct;		
		
		//keys: {distSim, colloFreq, colloFreqLocal, coordMatch, morphoMatch, semanticSim}
		Map heurResults = new HashMap();
		
		public CoordInstance(){}
		
		public CoordInstance(boolean pre, int[] bounds, int ncInd, int fcInd, int modInd, String nc, String fc, String mod, String[] pattern){
			this.pre = pre;
			this.boundaryIndexes = bounds;
			this.nearConjunctIndex = ncInd;
			this.farConjunctIndex = fcInd;
			this.modifierIndex = modInd;
			this.nearConjunct = nc;
			this.farConjunct = fc;
			this.modifier = mod;
			this.pattern = pattern;
		}
				
		public String getInstance(){
			String instance = "\"";
			for (int i = boundaryIndexes[0]; i <boundaryIndexes[1]; i++){
				instance += sentenceTokens[i];
				if (i < boundaryIndexes[1] -1)
					instance += " ";
			}
			instance += "\"";
			return instance;
		}
		
		public String[] getSentenceTokens(){return sentenceTokens;}		
		public String[] getSentencePOS(){return sentencePOS;}		
		public String[] getSentenceLemmas(){return sentenceLemmas;}
		public String getSentence(){return sentence;}
		
		/*
		 * The constructor and methods below are ONLY intended for use with training/test data!
		 */
		public CoordInstance(String mod, String nc, String fc, String mPOS, String ncPOS, String fcPOS, String mLemma, String ncLemma, String fcLemma, boolean pre, String Atype){
			
			this.modifier = mod;
			this.nearConjunct = nc;
			this.farConjunct = fc;
			String[] tags  = new String[]{mPOS, ncPOS, fcPOS};
			setPOS(tags);
			String[] lemmas = new String[]{mLemma, ncLemma, fcLemma};
			setLemmas(lemmas);
			modifierIndex = 0;
			nearConjunctIndex = 1;
			farConjunctIndex = 2;
			this.pre = pre;
			this.Atype = Atype;
		}		
		private void setPOS(String[] tags){
			sentencePOS = new String[]{tags[0],tags[1],tags[2]};
		}
		private void setLemmas(String[] lemmas){
			sentenceLemmas = new String[]{lemmas[0],lemmas[1],lemmas[2]};
		}
	}
	
	
	public static void main(String[] args){
		CoordSent test = new CoordSent();
		test.sentenceLemmas = new String[]{"This", "is", "a", "test"};
		test.addInstance();
		//CoordSent.CoordInstance instance = test.instances.get(0);
		//instance.CoordSent.this.getSentenceLemmas();
		String[] result = test.instances.get(0).getSentenceLemmas();
		int bloo = 9;
	}
}