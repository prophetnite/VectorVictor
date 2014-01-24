/* 
	nm 2014-01-14
	an object with a toJSON method to wrap and return ambiguity detector results 
	
	the resultset is the 'computationalObject' used on the client side to evaluate and indicate risk 
	to  the user and link to a pedagogy or curriculum



*/

import java.util.*;
import java.io.*;

import org.json.*;

public class coordAmbiguityResults{


	public int ccCount;
	public int patternCount;
	public List<observation> observations;
	public List<String> tokens;
	
	
	public class observation {
		public double nocuous;
		public double innocuous;
		public String modifier;
		public String nearConjunct;
		public String farConjunct;
		public int idxMod;
		public int idxNC;
		public int idxFC;
		
		public observation(){};
		
		public observation(double noc, double inoc, String mod , String snc, String sfc, int pmod, int pnc, int pfc){
			this.nocuous = noc;
			this.innocuous = inoc;
			this.modifier = mod;
			this.nearConjunct = snc;
			this.farConjunct = sfc;
			this.idxMod = pmod;
			this.idxNC = pnc;
			this.idxFC = pfc;
		};
	}
		
		
	public 	coordAmbiguityResults(){
		this.ccCount = 0;  		// coordintaing conjunctions limited to and | or currently
		this.patternCount = 0;	// matching Patterns limited to 10 POS patterns currently
		this.observations = new ArrayList<observation>(){};
		this.tokens = new ArrayList<String>(){};
	}	
	
	public 	coordAmbiguityResults(int a, int p, double n, double i){
		this.ccCount = a;
		this.patternCount = p;
		this.observations = new ArrayList<observation>(){};
		this.tokens = new ArrayList<String>(){};
	}	
	
	//TODO: this is lite and frree but evetually may need a JSON lib?
	public String toJSON(){
		StringBuilder sbjson = new StringBuilder("{\"ccCount\":" + this.ccCount + ", \"patternCount\":" + this.patternCount );
		if(this.tokens.size()>0){
			sbjson.append(", \"tokens\":[");
			for(String str: tokens){sbjson.append("\""+str+"\",");	}
		    sbjson.setLength(sbjson.length()-1); // strip last comma
		    sbjson.append("]");
		}
		if(this.observations.size()>0){
			sbjson.append(", \"obs\" : [");
			for (observation obs : this.observations){
				sbjson.append("{\"nocuous\":"+obs.nocuous + "," +
							   "\"innocuous\":"+obs.innocuous + "," +
							   "\"modifier\":\""+obs.modifier + "\"," +
							   "\"nearConjunct\":\""+obs.nearConjunct + "\"," +
							   "\"farConjunct\":\""+obs.farConjunct + "\"," +
							   "\"idxMod\":"+obs.idxMod + "," +
							   "\"idxNC\":"+obs.idxNC + "," +
							   "\"idxFC\":"+obs.idxFC + "},");
			}
			sbjson.setLength(sbjson.length()-1); // strip last comma
			sbjson.append("]");
		}
		return sbjson.append("}").toString();
	}
	
	
	
//	public String toString(){
//		return  "(coordintaingConjunctions) ccCount:"+ this.ccCount + "\n" +
//			    "(matchingPatterns) patternCount:"+ this.patternCount + "\n";
//	}


}