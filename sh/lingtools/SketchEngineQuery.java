import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.protocol.*;
import org.apache.commons.httpclient.contrib.ssl.*;
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
class SketchEngineQuery {
	
	static final String root_url = "beta.sketchengine.co.uk";
    static final String ske_username = "nilocm";
    static final String ske_password = "3gY6jw9REA";
            
    String method;
    List queryList = new ArrayList<HashMap>();    
    Map attrs = new HashMap();
    
    List<JSONObject> JSONresult = new ArrayList<JSONObject>();
    
    /*
     * SketchEngineQuery constructor:
     * 		method: http://www.sketchengine.co.uk/documentation/wiki/SkE/Methods/methods
     * 		corpus: generally "bnc2" or "ententen12_1"
     * 		queryList: formatting found at http://www.sketchengine.co.uk/documentation/wiki/SkE/Methods/methods
     */
	public SketchEngineQuery(String method, Map attrs, List<Map<String,String>> queryList){
		this.method = method;		
		this.attrs = attrs;
		this.queryList = queryList;
		runQuery();		
	}
	
	/* 
	 * return the query results
	 */
	public List<JSONObject> getResults(){
		return JSONresult;
	}
	
	/*
	 * run the desired query
	 * modified version of example3_ca.java from http://www.sketchengine.co.uk/documentation/wiki/SkE/Methods/authentication
	 * 
	 */
	public void runQuery(){
		System.out.println("runQuery Started");
		String base_url = "/bonito/run.cgi/";
        
        // make HTTPS connection
        HttpClient client = new HttpClient();
        try {
          Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory)new EasySSLProtocolSocketFactory(), 443));
          client.getHostConfiguration().setHost(root_url, 443, "https");
          client.getParams().setCookiePolicy(CookiePolicy.DEFAULT); //modified from original per: https://www.sketchengine.co.uk/documentation/ticket/693
        } catch (java.security.GeneralSecurityException e){
          e.printStackTrace();
        } catch (IOException e){
          e.printStackTrace();
        }
        client.getParams().setCookiePolicy(CookiePolicy.DEFAULT); //modified from original per: https://www.sketchengine.co.uk/documentation/ticket/693
        
        // retrieve session id
        GetMethod authget = new GetMethod("/login/");
        try {
            int code=client.executeMethod(authget);
        } catch (IOException ex) {
            System.err.println("Error: couldn't retrieve session ID from Sketch Engine server.");
            System.exit(1);
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
            System.exit(2);
        }
        authpost.releaseConnection();
      
        // retrieve data
        for (int i = 0; i < queryList.size(); i++) {
            System.out.print(i + " ");
        	try {
                attrs.putAll((HashMap)queryList.get(i));              
                JSONObject json_query = new JSONObject(attrs);
                String url_string = base_url + method + "?json=" + json_query.toString();
                GetMethod getJSON = new GetMethod(new URI(url_string, false).toString());
                client.executeMethod(getJSON);
                JSONObject json = new JSONObject(new BufferedReader(new InputStreamReader (getJSON.getResponseBodyAsStream())).readLine());
                JSONObject jsonCopy = new JSONObject(json, JSONObject.getNames(json));  //from: http://stackoverflow.com/questions/12809779/how-do-i-clone-an-org-json-jsonobject-in-java
                JSONresult.add(jsonCopy);
                getJSON.releaseConnection();
                System.out.println("working");
            } catch (URIException ex) {
                System.err.println("Error: malformed URI in request.");
            } catch (JSONException ex) {
                System.err.println("Error: malformed JSON format.");
            } catch (IOException ex) {
                System.err.println("Error: couldn't retrieve JSON data from Sketch Engine server.");
            }
        }
        System.out.println();
	}
}

