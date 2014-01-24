import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.ScoredObject;
import edu.stanford.nlp.trees.tregex.*;
/*
 * Future edits to suck less and actually make sense.
 */
public class TreeMatcherDriver {
    private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
    //private final static int DIFFERENCE_THRESHOLD = 2;
    
    private final static boolean isTest = true;
    
    //(ROOT (S (VP (VB Have) (NP (DT) (JJ) (NN) (CC) (NN))) (. !))) -65.62038230895996
    //(ROOT (S (VP (VB Have) (NP (NP (DT) (JJ) (NN)) (CC) (NP (VBG)))) (. !))) -65.94569796323776
    private final static TregexPattern[][] compSets = {{TregexPattern.compile("/^VP/ < (/^NP/ $+ (/^PP/< (IN : PP)))"), 
    												  TregexPattern.compile("/^VP/ < (/^NP/ < (/^NP/ $+ /^PP/))")},//vp (np pp) vs. vp (np (np pp)) // catches 'robber with the stick' type sentence
    												  {TregexPattern.compile("/^NP/ << (/^JJ/ $++ /^N*/ $++ /^CC*/ $++ /^N*/)"),//(NP (DT a) (JJ great) (NN method) (CC and)(NN madness)))
    												  TregexPattern.compile("/^NP/ << ((/^NP/ < /^JJ/ < /^N*/) $++ /^CC*/ $++ /^N*/)")}};
    
    private static List<List<Tree>> biguous = new ArrayList<List<Tree>>();
	public static void main(String[] args) throws IOException {
	    //PrintWriter xmlOut = null;
	    //if (args.length > 2) xmlOut = new PrintWriter(args[2]);
	    

		Properties props = new Properties();
		
		props.put("annotators","tokenize,ssplit");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);
		
		// ==== nm 20131217 website testbed feeds annotations text as a commandline arg
		Annotation annotation;
		
		annotation = new Annotation(args[0]); // TODO: validate input text, prep white space line feeds, paragraphs etc...
		/*------------
	    if(isTest) {
	    	annotation = new Annotation("Then he robbed the store with the ocelot. "  
	    	      + "Do you enjoy drinking eggnog or milk with James Joyce? "
	    		  + "I have a confession to make. "
	     		  + "This sentence contains a few words, which are represented in text by letters. "
	    		  + "The policeman chased the burglar with a stick. " 
	     		  + "The hamburger chased the burglar with cheese. "
	     		  + "Have a happy day and bookbag! "
	    		  + "The TM304 is a novel device technically specified in the following pages. "
	    		  + "One object of the present invention is to implement a software development tool set on a computer platform that enhances the comprehension of "
	    		  +"requirements or other documents by more clearly presenting the most relevant "
	    		  +"concepts. The VTAS tool set will provide the capacity to develop more accurate and " 
	    		  +"complete requirements and design specifications for software programs and other "
	    		  +"technology products which will result in faster implementation and greater "
	    		  +"alignment of the user-interface and the operational capabilities of, for example, a " 
	    		  +"software product, or improvements in manufacturability or training and use of a "
	    		  +"technology product according to the design specifications, with measurable fidelity "
	    		  +"to the company\'s express specifications.");
	    } else {
	    	annotation = new Annotation(IOUtils.slurpFileNoExceptions("sentences.txt"));
	    }
	      ----------- */
		  
	  	LexicalizedParserQuery lpq = parser.lexicalizedParserQuery();
	    pipeline.annotate(annotation);
	    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    
	    if (sentences != null && sentences.size() > 0) {
	    	for(CoreMap sentence : sentences) {//each sentence
	    		
	    		List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
	    		lpq.parse(tokens);
	    		List<ScoredObject<Tree>> trees = lpq.getKBestPCFGParses(3);
	    		double differentness = rateDifferentness(trees)/(Math.pow(tokens.size()*trees.size(),1.5)); 
    			
	    		if(differentness>0.01) {
	    			biguous = new ArrayList<List<Tree>>();
	    			for(int i = 0; i < compSets.length; ++i) {
	    				List<Tree> lt = compareTrees(trees, compSets[i]);
	    				if(!lt.isEmpty())biguous.add(lt);
	    			}	

	    	    	System.out.println("_______________________________________");
	    		    System.out.println(sentence.toString());
	    		    /*for(ScoredObject<Tree> st: trees) {
	    		    	System.out.println(st.object().toString() + " " + st.score());
	    		    }*/
	    		    
	    		    for(List<Tree> lt : biguous) {
	    		    	for(Tree t : lt) {
	    		    		System.out.println(t.toString());
	    		    	}
	    		    	System.out.println();
	    		    }
	    		    System.out.println("differentness rating: " + differentness);
	    		}
	    	}	
	    } 
	}
	
	//compares a list of scored parses for the same sentence.
	public static List<Tree> compareTrees(List<ScoredObject<Tree>> scoredTrees, TregexPattern[] patternSet) throws UnsupportedOperationException{
		
		Iterator<ScoredObject<Tree>> iter = scoredTrees.iterator();
		ArrayList<Tree> trees = new ArrayList<Tree>();
		while(iter.hasNext()) {
			trees.add(iter.next().object());
		}
		ArrayList<Tree> matches0 = new ArrayList<Tree>(), matches1 = new ArrayList<Tree>();
		
		//we want to compare the two match types, so first collect them.   throw 'em all together
		for(Tree tree : trees) {
			TregexMatcher tm0 = patternSet[0].matcher(tree), tm1 = patternSet[1].matcher(tree);
			while(tm0.findNextMatchingNode()) {
				matches0.add(tm0.getMatch());
			}
			while(tm1.findNextMatchingNode()) {
				matches1.add(tm1.getMatch());
			}
		}
		List<Tree> ambiguousTrees = new ArrayList<Tree>();
		//compare matches for the two patterns
		for(Tree match0: matches0) {		
			for(Tree match1: matches1) {
				if(!match0.equals(match1) && match0.getLeaves().equals(match1.getLeaves())) {
					if(!ambiguousTrees.contains(match0) && !ambiguousTrees.contains(match1) ) {
						//System.out.println(match0.getLeaves().toString() + " fits patterns " + patternSet[0].toString() + " and " + patternSet[1].toString());
						ambiguousTrees.add(match0);
						ambiguousTrees.add(match1);
						
					}
					
				}
			}
		}
		
		return ambiguousTrees;
	}
	
	//precondition: tree1 and tree2 have equivalent leaves (i.e. derived from the same sentence/phrase)
	//right now, bottom-up assessment.  start at leaves, check if ancestors different.
	public static int rateDifferentness(List<ScoredObject<Tree>> scoredTrees) {
		int rating = 0;
		for(ScoredObject<Tree> stree1:scoredTrees) {
			for(ScoredObject<Tree> stree2:scoredTrees) {
				if(stree1.equals(stree2))continue;
				Tree tree1 = stree1.object();
				Tree tree2 = stree2.object();
				List<Tree> leaves1 = tree1.getLeaves();
				List<Tree> leaves2 = tree2.getLeaves();
				
				for(int k = 0; k<leaves1.size(); k++) {//for each leaf in the tree
					try {
						for(int i = 0; i < tree1.depth(); i++){ //go up until a difference has been hit.
							if(!leaves1.get(k).ancestor(i, tree1).equals(leaves2.get(k).ancestor(i, tree2))){
								rating++;
								break;
							}
						}
					} catch(Exception e){}//System.out.println(e.getMessage());}
				}
			}
		}
		return rating;
	}
}