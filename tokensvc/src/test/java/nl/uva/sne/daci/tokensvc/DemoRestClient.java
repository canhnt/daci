package nl.uva.sne.daci.tokensvc;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
public class DemoRestClient {
	
	String TENANT_ID = "daci_tenant_id";
	String REQUEST = "daci_request";
	String KEYINFO = "daci_keyinfo";
	
	public static void main(String[] args) {
        DemoRestClient restClient = new DemoRestClient();
        try {
//            restClient.addBook("Naked Sun", "Issac Asimov");
            restClient.issueGrantToken("VI123", "abc123doremi", "000111");
        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }

	}

	private void issueGrantToken(String tenantId, String request, String keyinfo) throws Exception {
		String output = null;
        try{
            String url = " http://localhost:8080/tokenservice/grant_token";
            HttpClient client = new HttpClient();
            PostMethod mPost = new PostMethod(url);
            mPost.addParameter(TENANT_ID, tenantId);
            mPost.addParameter(REQUEST, request);
            mPost.addParameter(KEYINFO, keyinfo);
            Header mtHeader = new Header();
            mtHeader.setName("content-type");
            mtHeader.setValue("application/x-www-form-urlencoded");
            mtHeader.setName("accept");
            mtHeader.setValue("application/xml");
            //mtHeader.setValue("application/json");
            mPost.addRequestHeader(mtHeader);
            client.executeMethod(mPost);
            output = mPost.getResponseBodyAsString( );
            mPost.releaseConnection( );
            System.out.println("output : " + output);
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e);
        }
		
	}

	 public void addBook(String bookName, String author) throws Exception {

	        String output = null;
	        try{
	            String url = "http://localhost:8080/Weblog4jDemo/bookservice/addbook";
	            HttpClient client = new HttpClient();
	            PostMethod mPost = new PostMethod(url);
	            mPost.addParameter("name", "Naked Sun");
	            mPost.addParameter("author", "Issac Asimov");
	            Header mtHeader = new Header();
	            mtHeader.setName("content-type");
	            mtHeader.setValue("application/x-www-form-urlencoded");
	            mtHeader.setName("accept");
	            mtHeader.setValue("application/xml");
	            //mtHeader.setValue("application/json");
	            mPost.addRequestHeader(mtHeader);
	            client.executeMethod(mPost);
	            output = mPost.getResponseBodyAsString( );
	            mPost.releaseConnection( );
	            System.out.println("output : " + output);
	        }catch(Exception e){
	        throw new Exception("Exception in adding bucket : " + e);
	        }

	    }
	
}
