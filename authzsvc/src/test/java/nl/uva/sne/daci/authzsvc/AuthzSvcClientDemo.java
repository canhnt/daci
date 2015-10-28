package nl.uva.sne.daci.authzsvc;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import nl.uva.sne.daci.authzsvc.AuthzSvc.DecisionType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class AuthzSvcClientDemo {
	
	private static final String AUTHZSVC_URL = "http://localhost:8181/cxf/authzservice/";

	private static final String METHOD = "/authorize";
	
	public static final String SUBJECT_ROLE_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-role";
	
	public static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	public static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	
	public static final String[][] REQUESTS = new String[][]{
		{"http://demo3.uva.nl/vi/861/", "admin", "http://demo3.uva.nl/vi/861/ComputingNode", "SLI:Operate-VR:Stop"},		
	};

	public static String serviceUrl;

	public AuthzSvcClientDemo(){
	}
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("authzclientdemo <authzsvc-url>");
			return;
		}
		
		serviceUrl = args[0];
		System.out.println("Connecting to " + serviceUrl);
		
		try {
			AuthzSvcClientDemo client = new AuthzSvcClientDemo();
			int numPermit = 0;
			long startTime = System.currentTimeMillis();
			
			for(int k = 0; k < 100; k++) {
				for(int i = 0; i < REQUESTS.length-1; i++){
					AuthzRequest request = createRequest(REQUESTS[i][1], REQUESTS[i][2], REQUESTS[i][3]);
					
					String tenantId = REQUESTS[i][0];
					AuthzResponse response = client.authorize(tenantId, request);					
					if (response != null) {
						if (response.getDecision()  == DecisionType.PERMIT){
							numPermit++;
							String token = response.getToken();
							if (token != null && !token.isEmpty()) {
								System.out.println("Token: " + token);
							}
						} 
					}						
				}
			}
			long stopTime = System.currentTimeMillis();
			System.out.println("Runtime:" + (stopTime - startTime) + "(ms)");
			System.out.println("# of permits:" + numPermit);

		}catch(Exception e) {
			e.printStackTrace();
		}		
	}

	private static AuthzRequest createRequest(String subjectRole, String resourceId, String actionId) {
		AuthzRequest request = new AuthzRequest();
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(SUBJECT_ROLE_ID, subjectRole);
		attrs.put(RESOURCE_ID, resourceId);
		attrs.put(ACTION_ID, actionId);
		request.setAttributes(attrs);
		return request;
	}

	private AuthzResponse authorize(String tenantId, AuthzRequest request) throws UnsupportedEncodingException {
		Client client = Client.create();
		WebResource webResource = client.resource(this.serviceUrl + URLEncoder.encode(tenantId, "UTF-8") + METHOD);
		
//		AuthzResponse resp = webResource.type("application/xml").post(AuthzResponse.class, request);			
		webResource.type(MediaType.APPLICATION_XML);
		webResource.accept(MediaType.APPLICATION_XML);
		
		ClientResponse clientResponse = webResource.post(ClientResponse.class, request);
		
//		System.out.println("Status:" + clientResponse.getStatus());
		if (clientResponse.getStatus() >= 200) {
//			System.out.println("POST succeeded");
			return clientResponse.getEntity(AuthzResponse.class);
		}
		else {
//			System.err.println("POST failed");
			return null;
		}	
	}
	
	public static String marshalResponse(JAXBContext jaxbContext, AuthzResponse resp) throws JAXBException {
		StringWriter writerTo = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 
	    marshaller.marshal(resp, writerTo); //create xml string from the input object
	    
		return writerTo.toString();
	}
}
