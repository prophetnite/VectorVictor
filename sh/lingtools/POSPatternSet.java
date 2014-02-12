import java.util.HashMap;
import java.util.Map;

public class POSPatternSet {
	
	PatternType[] patternList = new PatternType[4];
	
	
	public POSPatternSet(){  
		
		this.patternList[0] = new PatternType(new HashMap<String, Integer>() {{put("M", 1);put("NC", 3);put("FC", 5);}},new String[][]{{"DT|JJ.*", "NN.*", "IN", "NN.*", "CC", "NN.*"}}, 4);    //"the SET of PLANS and TABLES"
		
	
		this.patternList[1] = new PatternType(new HashMap<String, Integer>() {{put("M", 5);put("NC", 2);put("FC", 0);}}, new String[][]{{"NN.*", "CC", "NN.*", "IN", "DT|JJ.*", "NN.*"},    //"BOOK and PAPER on the TABLE"
																										{"VB.*", "CC", "VB.*", "IN", "DT|JJ.*", "NN.*"}}, 1);  //"IMPLEMENTED and EXECUTED on the PLATFORM"
								
		this.patternList[2] = new PatternType(new HashMap<String, Integer>() {{put("M", 0);put("NC", 1);put("FC", 3);}}, new String[][]{{"JJ.*", "NN.*", "CC", "NN.*"},    //"MANUAL INPUT and OUTPUT"
																								{"VBN", "NN.*", "CC", "NN.*"},    //"ASSOCIATED DOORS and WINDOWS"
																								{"NN.*", "NN.*", "CC", "NN.*"},    //"PROJECT MANAGER and DESIGNER"
																								{"RB.*", "VB.*", "CC", "VB.*"}}, 2);  //"be MANUALLY REJECTED AND FLAGGED"  		
		
		this.patternList[3] = new PatternType(new HashMap<String, Integer>() {{put("M", 3);put("NC", 2);put("FC", 0);}}, new String[][]{{"NN.*", "CC", "NN.*", "NN.*"},    //"SOFTWARE and HARDWARE PRODUCT"
																								{"VB.*", "CC", "VB.*", "NN.*"},    //"GENERATE and PRINT REPORTS"
																								{"VB.*", "CC", "VB.*", "RB.*"}}, 1);  //"be INSPECTED and RECORDED AUTOMATICALLY"	
	}	
}