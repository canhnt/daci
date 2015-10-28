package nl.uva.sne.daci.contextsvc.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;

import nl.uva.sne.daci._1_0.schema.GrantTokenType;
import nl.uva.sne.daci._1_0.schema.ObjectFactory;
import nl.uva.sne.daci._1_0.schema.RequestType;
import nl.uva.sne.daci.utils.XMLUtil;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3._2000._09.xmldsig.KeyInfoType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TokenSvcClient {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenSvcClient.class);

	private static final String LOCAL_TOKEN_SERVICE_URL = "http://localhost:8080/tokenservice";
	
	public static final String METHOD_ISSUE_GRANT_TOKEN = "/grant_token";
	
	public static final String METHOD_VERIFY_GRANT_TOKEN = "/verify_grant_token";
	
	public static final String TENANT_ID = "daci_tenant_id";
	
	public static final String REQUEST = "daci_request";
	
	public static final String KEYINFO = "daci_keyinfo";
	
	public static final String TOKEN = "daci_token";
	
	
	public static final String SAMPLE_REQUEST_FILE = "src/test/resources/sample-request.xml";
	
	public static final String SAMPLE_KEYINFO_FILE = "src/test/resources/sample-keyinfo.xml";
	
	public static void main(String[] args) throws Exception {
		
        TokenSvcClient client = new TokenSvcClient(LOCAL_TOKEN_SERVICE_URL);
        
        Document docRequest = XMLUtil.readXML(SAMPLE_REQUEST_FILE);
        RequestType request = XMLUtil.unmarshal(RequestType.class, docRequest.getDocumentElement());
        
        Document docKeyInfo = XMLUtil.readXML(SAMPLE_KEYINFO_FILE);
        KeyInfoType keyInfo = XMLUtil.unmarshal(KeyInfoType.class, docKeyInfo.getDocumentElement());
        
//        GrantTokenType t = client.issueGrantToken("abc123", request, keyInfo);
        
        String s = client.issueGrantToken("abc123", request, keyInfo);
        System.out.println("Received token:" + s);
        
//        print(t, System.out);
//        String sToken = convertGrantToken(t);
//        System.out.println("Received token:" + sToken);
        GrantTokenType t = convertGrantToken(s);
        System.out.println("Converted token:");print(t, System.out);
        
        System.out.println("2nd converted token:" + convertGrantToken(t));
        
    	client.verifyGrantToken(s);                        
	}
	
	private static String convertGrantToken(GrantTokenType t) throws JAXBException {
		

		OutputStream os = new ByteArrayOutputStream();
		
		JAXBElement<GrantTokenType> jaxb = (new ObjectFactory()).createGrantToken(t);
		
		JAXBContext jc = JAXBContext.newInstance(GrantTokenType.class);
		Marshaller m = jc.createMarshaller();
		m.marshal(jaxb, os);
				
		return os.toString();
	}

	private static void print(GrantTokenType t, OutputStream os) {
				
		JAXBElement<GrantTokenType> jaxb = (new ObjectFactory()).createGrantToken(t); 
		XMLUtil.print(jaxb, GrantTokenType.class, os);		
	}

	private String serviceURL;
	private HttpClient httpClient;

	private String methodIssueGrantTokenURL;

	private String methodVerifyGrantTokenURL; 

	public TokenSvcClient(String address){
		this.serviceURL = address;
		
		this.methodIssueGrantTokenURL = this.serviceURL + METHOD_ISSUE_GRANT_TOKEN;
		
		this.methodVerifyGrantTokenURL = this.serviceURL + METHOD_VERIFY_GRANT_TOKEN;
		
		
		httpClient = new HttpClient();
	}
	
	public boolean verifyGrantToken(String token) throws Exception {

		PostMethod mPost = new PostMethod(this.methodVerifyGrantTokenURL);
				
        mPost.addParameter(TOKEN, URLEncoder.encode(token, "UTF-8"));
        Header mtHeader = new Header();
        mtHeader.setName("content-type");
        mtHeader.setValue("application/x-www-form-urlencoded");
        mtHeader.setName("accept");
        mtHeader.setValue("application/xml");
        mPost.addRequestHeader(mtHeader);
     
        try {
			httpClient.executeMethod(mPost);
			
			String output = mPost.getResponseBodyAsString();
			log.info("Verification result:" + output);
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("Exception in connecting to tokenservice at " + this.serviceURL);
		} finally {
			mPost.releaseConnection();
		}                
	}
	
	/**
	 * 
	 * @param tenantId
	 * @param request
	 * @param userKeyInfo
	 * @return The string in pure form (no encoding) representing a grant token. 
	 * @throws Exception
	 */
	public String issueGrantToken(String tenantId, RequestType request, KeyInfoType userKeyInfo) throws Exception {
		
		String requestParam = print(request);
		String keyInfoParam = print(userKeyInfo);
		
        PostMethod mPost = new PostMethod(this.methodIssueGrantTokenURL);
        mPost.addParameter(TENANT_ID, tenantId);
        mPost.addParameter(REQUEST, requestParam);
        mPost.addParameter(KEYINFO, keyInfoParam);
        
        Header mtHeader = new Header();
        mtHeader.setName("content-type");
        mtHeader.setValue("application/x-www-form-urlencoded");
        mtHeader.setName("accept");
        mtHeader.setValue("application/xml");
        //mtHeader.setValue("application/json");
        mPost.addRequestHeader(mtHeader);
                
        try {
			httpClient.executeMethod(mPost);
			
			String output = mPost.getResponseBodyAsString();
			if (output != null && !output.isEmpty()) {				
//				return convertGrantToken(output);
				return URLDecoder.decode(output, "UTF-8");
			} else {
				log.error("Receive nothing");
				return null;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("Exception in connecting to tokenservice at " + this.serviceURL);
		} finally {
			mPost.releaseConnection();
		}        
	}

	/**
	 * 
	 * @param token A grant token in pure string. 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static GrantTokenType convertGrantToken(String token) throws ParserConfigurationException, SAXException, IOException {

		InputStream istream = new ByteArrayInputStream(token.getBytes("UTF-8"));

		// From IS -> DOM -> JAXB -> Object	
		Document doc = XMLUtil.readXML(istream);

		return XMLUtil.unmarshal(GrantTokenType.class, doc.getDocumentElement());		
	}
	
	
	private static String print(RequestType request) {		
		OutputStream os = new ByteArrayOutputStream();				
		XMLUtil.print((new ObjectFactory()).createRequest(request), RequestType.class, os);		
		return os.toString();
	}


	private static String print(KeyInfoType keyInfo) {
		OutputStream os = new ByteArrayOutputStream();				
		XMLUtil.print((new ObjectFactory()).createKeyInfo(keyInfo), KeyInfoType.class, os);		
		return os.toString();
	}
}
