package nl.uva.sne.daci.benchmark;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.core.MediaType;

import nl.uva.sne.daci.authzsvc.AuthzRequest;
import nl.uva.sne.daci.authzsvc.AuthzResponse;
import nl.uva.sne.daci.authzsvc.AuthzSvc.DecisionType;

import org.apache.cxf.jaxrs.client.WebClient;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class AuthzSvcClient implements Runnable{
	
//	private static final String DEFAULT_ADDRESS = "http://localhost:8181/cxf/authzservice/";;

	private static final String METHOD = "/authorize";
	
	public static final String SUBJECT_ROLE_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-role";
	
	public static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	public static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	
//	public static final String TENANT_ID_FILE = "src/man/resources/tenants-1k.txt";		
		
	public static final String RESOURCE_ID_VALUE_TEMPLATE = "%sComputingNode";
	
	public static final String ACTION_ID_VALUE = "SLI:Operate-VR:Stop";
	
	public static final String ROLE_ID_VALUE = "admin";

	public static final int NUM_REQUESTS_PER_TENANT = 10;
	
	///////////////////////////////////////////
	
	private String authzSvcAddress;

	private int numPermitResponses;

	private int numNAResponses;

	private int numDenyResponses;

	private int numNullRespones;

	private int numErrorResponses;

	private int numUnknownResponses;
	
	public int nTotalRequests;
	
	// Each tenant has a set of random requests.
	private Map<String, List<AuthzRequest>> mapTenants2Requests;

	private List<String> tenantIds;

	private final CountDownLatch latch;

	private long nExecTime;

	private String name;

	private Client client;
		
	public AuthzSvcClient(CountDownLatch latch, List<String> tenantIds, String authzSvcAddress, String threadName) {
		this.latch = latch;
		
		this.authzSvcAddress = authzSvcAddress;
		this.name = threadName;
		this.tenantIds = tenantIds;
		
		numPermitResponses = 0;
		numNAResponses = 0;
		numDenyResponses = 0;
		numNullRespones = 0;
		numErrorResponses = 0;
		numUnknownResponses = 0;
	}


	public long getExecTime(){
		return this.nExecTime;
	}
	
	public long getTotalRequests() {		
		return nTotalRequests;
	}
	
	public long getNumErrorResponses(){
		return this.numErrorResponses;
	}
	
	public long getNumNullRespones(){
		return this.numNullRespones;
	}
	
	public long getNumDenyResponses(){
		return this.numDenyResponses;
	}

	public long getNumNAResponses(){
		return this.numNAResponses;
	}
	
	public long getNumPermitResponses(){
		return this.numPermitResponses;
	}
	
	public long getNumUnknownResponses(){
		return this.numUnknownResponses;
	}

//	private void printResults() {
//		System.out.println("# Permit requests	:" + numPermitResponses);
//		System.out.println("# NA requests	 	:" + numNotApplicableResponses);
//		System.out.println("# Deny requests		:" + numDenyResponses);
//		System.out.println("# Null requests		:" + numNullRespones);
//		System.out.println("# Error requests	:" + numErrorResponses);
//		System.out.println("# Unknown requests	:" + numUnknownResponses);		
//	}

	/**
	 * Prepare requests
	 * @throws FileNotFoundException 
	 */
	public void init() throws Exception {
		this.client = Client.create();
		
		mapTenants2Requests = new HashMap<String, List<AuthzRequest>>(tenantIds.size());
		
		for(int i = 0; i < tenantIds.size(); i++) {

			String trustorId = tenantIds.get(i);
			String trusteeId = tenantIds.get((i+1) % tenantIds.size());
			
			// request to private resources of the tenant trusteeId
			List<AuthzRequest> directRequests = generateDirectRequests(trusteeId, NUM_REQUESTS_PER_TENANT / 2);			
			
			// requests to shared resources from trustor
			List<AuthzRequest> indirectRequests = generateIndirectRequests(trustorId, trusteeId, NUM_REQUESTS_PER_TENANT/2);
			
			List<AuthzRequest> allRequests = new ArrayList<AuthzRequest>(directRequests.size() + indirectRequests.size());
			allRequests.addAll(directRequests);
			allRequests.addAll(indirectRequests);
			
			mapTenants2Requests.put(trusteeId, allRequests);
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
		WebResource webResource = client.resource(authzSvcAddress + URLEncoder.encode(tenantId, "UTF-8") + METHOD);
		
		WebResource.Builder builder = webResource.getRequestBuilder(); 
		builder.accept(MediaType.APPLICATION_XML);
		builder.type(MediaType.APPLICATION_XML);
		
		ClientResponse clientResponse = builder.post(ClientResponse.class, request);
		
		if (clientResponse.getStatus() == 200) {
			return clientResponse.getEntity(AuthzResponse.class);
		}
		else {
			return null;
		}	
	}
	
	private AuthzResponse cxfAuthorize(String tenantId, AuthzRequest request) throws UnsupportedEncodingException {
		String url = authzSvcAddress + URLEncoder.encode(tenantId, "UTF-8") + METHOD;
		WebClient client = WebClient.create(url);
		client.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML);
		AuthzResponse resp = client.post(request, AuthzResponse.class);
		
		return resp;
	}

	private static List<AuthzRequest> generateDirectRequests(String tenantId, int numRequests) {
		List<AuthzRequest> requests = new ArrayList<AuthzRequest>(numRequests);
		
		String resourceId = String.format(RESOURCE_ID_VALUE_TEMPLATE, tenantId);
		AuthzRequest request = createRequest(ROLE_ID_VALUE, resourceId, ACTION_ID_VALUE);

		for(int i = 0; i < numRequests; i++) {
			requests.add(request);
		}
			
		return requests;
	}

	private static List<AuthzRequest> generateIndirectRequests(String trustorId, String trusteeId, int numRequests) {
		List<AuthzRequest> requests = new ArrayList<AuthzRequest>(numRequests);
		
		String resourceId = String.format(RESOURCE_ID_VALUE_TEMPLATE, trustorId);
		AuthzRequest request = createRequest(ROLE_ID_VALUE, resourceId, ACTION_ID_VALUE);
		
		for(int i = 0; i < numRequests; i++) {
			requests.add(request);
		}			
		return requests;
	}

	@Override
	public void run() {		
		try{
			long startTime = System.currentTimeMillis();			
			
			sendAllRequests();
	
			long stopTime = System.currentTimeMillis();
			
			this.nExecTime = stopTime - startTime;
			
			this.nTotalRequests = 0;
			for(String tenantId: mapTenants2Requests.keySet()) {
				this.nTotalRequests += mapTenants2Requests.get(tenantId).size();
			}				
			System.out.println(this.name + " has completed");
		}finally{
			latch.countDown();
		}		
	}
	
	private void sendAllRequests() {
		
		for(String tenantId : mapTenants2Requests.keySet()) {
			
			// send all requests of this tenant to the remote authzsvc
			for(AuthzRequest request : mapTenants2Requests.get(tenantId)) {
				
				AuthzResponse resp = null;
				try {
					resp = authorize(tenantId, request);
//					resp = cxfAuthorize(tenantId, request);					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (resp == null)
					numNullRespones++;
				else if (resp.getDecision() == DecisionType.PERMIT)
					numPermitResponses++;
				else if (resp.getDecision() == DecisionType.NOT_APPLICABLE)
					numNAResponses++;
				else if (resp.getDecision() == DecisionType.DENY)
					numDenyResponses++;
				else if (resp.getDecision() == DecisionType.ERROR)
					numErrorResponses++;
				else
					numUnknownResponses++;
			}
		}			
		
	}

}
