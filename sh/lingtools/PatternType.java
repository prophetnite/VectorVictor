import java.util.*;

public class PatternType {
	Map<String, Integer> labels; 
	String[][] patterns;
	int andIndex;
		
	public PatternType(HashMap<String, Integer> labels, String[][]patterns, int andIndex){
		this.labels = labels;
		this.patterns = patterns;
		this.andIndex = andIndex;
	}
}