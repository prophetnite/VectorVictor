
/*
	SketchEngineQueryObj is a refactored verison of Colins SketchEngineQuery class.
	- it provdes a login() function that can be claaed externlly.
	- it provides an HTTP 'client' member that persists the connection to SketchEngine
	- it provides a runQuery() method:
		- that takes a (String) method, (Map) attrs, (List<Map<String,String>>) queryList.
		- that returns a List<JSONObject> JSONresult.
			- example usage: "List<JSONObject> distSimList = sketchengine.runQuery("thes", thesAttrs, distSimQueries);"

*/

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.protocol.*;
import org.apache.commons.httpclient.contrib.ssl.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

import org.json.*;
import java.util.*;
import java.io.*;

/*
 * Should make batch querying easier. modified from code at: http://www.sketchengine.co.uk/documentation/wiki/SkE/Methods/authentication
 * Colin, 11/22/13
 * Requires the following libraries:
 * 	Apache commons-httpclient version 3.1
 *	Apache commons-codec
 *	Apache commons-logging
 *	Apache not-yet-commons-ssl
 *	org.JSON
 */
class SketchEngineQueryObj {
	
	public static long startTime; 				// = System.currentTimeMillis();
	public static long stopTime;  				// = System.currentTimeMillis();
	public static HttpClient client;	  		// login() instantiates client - do this before using.
	public static GetMethod httpmethod;
	static final String base_url = "/bonito/run.cgi/";
	static final String root_url = "beta.sketchengine.co.uk";
    static final String ske_username = "nilocm";
    static final String ske_password = "3gY6jw9REA";
    String method;
    List queryList = new ArrayList<HashMap>();    
    Map attrs = new HashMap();
	
   // REMOVE List<JSONObject> JSONresult = new ArrayList<JSONObject>();

     // 		method: http://www.sketchengine.co.uk/documentation/wiki/SkE/Methods/methods
     // 		corpus: generally "bnc2" or "ententen12_1"
     // 		queryList: formatting found at http://www.sketchengine.co.uk/documentation/wiki/SkE/Methods/methods
    
    public SketchEngineQueryObj(){};
  
	
	//Logon and authentication - sets 'client' which is used in runQuery();
	public boolean login(){
		String base_url = "/bonito/run.cgi/";
        // make HTTPS connection
        client = new HttpClient();
        try {
          Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory)new EasySSLProtocolSocketFactory(), 443));
          client.getHostConfiguration().setHost(root_url, 443, "https");
          client.getParams().setCookiePolicy(CookiePolicy.DEFAULT); //modified from original per: https://www.sketchengine.co.uk/documentation/ticket/693
        } catch (java.security.GeneralSecurityException e){
          e.printStackTrace(); return false;
        } catch (IOException e){
          e.printStackTrace(); return false;
        }
        client.getParams().setCookiePolicy(CookiePolicy.DEFAULT); //modified from original per: https://www.sketchengine.co.uk/documentation/ticket/693
        // retrieve session id
        GetMethod authget = new GetMethod("/login/");
        try {
            int code=client.executeMethod(authget);
        } catch (IOException ex) {
            System.err.println("Error: couldn't retrieve session ID from Sketch Engine server.");
            return false;
        }
        authget.releaseConnection();
        
        // login    
        PostMethod authpost = new PostMethod("/login/");
        NameValuePair submit   = new NameValuePair("submit", "ok");
        NameValuePair username = new NameValuePair("username", ske_username);
        NameValuePair password = new NameValuePair("password", ske_password);
        authpost.setRequestBody(new NameValuePair[] {submit, username, password});
           try {
             int code=client.executeMethod(authpost);
        } catch (IOException ex) {
            System.err.println("Error: couldn't login to Sketch Engine server.");
            return false;
        }
        authpost.releaseConnection();
		System.out.println("SketchEngineQueryObj logged in.");
        return true;
	}
	
	/*
	 * run the desired query
	 * modified version of example3_ca.java from http://www.sketchengine.co.uk/documentation/wiki/SkE/Methods/authentication
	 * nm 2014-01 refactored as function from SketchEngineQuery - part of objectification.
	 */
	public List<JSONObject> runQuery(String method, Map attrs, List<Map<String,String>> queryList){

		System.out.println("SketchEngineQueryObj runQuery() Started.");
		List<JSONObject> JSONresult = new ArrayList<JSONObject>();
		startTime = System.currentTimeMillis();
        for (int i = 0; i < queryList.size(); i++) {
            try {
                attrs.putAll((HashMap)queryList.get(i));              
                JSONObject json_query = new JSONObject(attrs);
                String url_string = base_url + method + "?json=" + json_query.toString();
                GetMethod getJSON = new GetMethod(new URI(url_string, false).toString());
                getJSON.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, true));
        		System.out.println(i + " working.");
                client.executeMethod(getJSON);
                JSONObject json = new JSONObject(new BufferedReader(new InputStreamReader (getJSON.getResponseBodyAsStream())).readLine());
                JSONObject jsonCopy = new JSONObject(json, JSONObject.getNames(json));  //from: http://stackoverflow.com/questions/12809779/how-do-i-clone-an-org-json-jsonobject-in-java
                JSONresult.add(jsonCopy);
                getJSON.releaseConnection();
            } catch (URIException ex) {
                System.err.println("Error: malformed URI in request.");
            } catch (JSONException ex) {
                System.err.println("Error: malformed JSON format.");
            } catch (IOException ex) {
                System.err.println("Error: couldn't retrieve JSON data from Sketch Engine server.");
            }
        }
        System.out.println("SketchEngine runQuery():" + (System.currentTimeMillis()- startTime));
		return JSONresult;
	}
}

