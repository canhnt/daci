package nl.uva.sne.daci.contextsvc;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.uva.sne.daci.context.ContextRequest;
import nl.uva.sne.daci.context.ContextResponse;
import nl.uva.sne.daci.context.ContextStore;
import nl.uva.sne.daci.context.PolicySetupUtil;
import nl.uva.sne.daci.context.ContextResponse.ContextDecision;
import nl.uva.sne.daci.context.impl.ContextManager;
import nl.uva.sne.daci.context.impl.ContextRequestImpl;
import nl.uva.sne.daci.contextsvc.impl.Configuration;
import nl.uva.sne.daci.contextsvc.impl.ContextSvcImpl;
import nl.uva.sne.daci.contextsvc.tenant.TenantManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

public class ContextSvcImplTest {
	public static final String REDIS_SERVER_ADDRESS = "localhost";
	public static final String DOMAIN = "demo3-sne";
	
	private static final String TRANSFER_POLICY_FILE = "src/test/resources/transfer-policies.xml";
//	private static final int NUM_ROOT_CONTEXTS = 3;
	
	private static final String INTER_TENANT_POLICY_FILE = "src/test/resources/inter-tenant-policies.xml";	
//	private static final int NUM_TENANT_CONTEXTS = 4;
	
	
	private static final String SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";	
	
	private static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	private static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";

	List<ContextRequest> ctxRequests;
	
	private ContextSvcImpl ctxSvc;
	
	private ArrayList<String> redisInsertedKeys;
	
	@Before
	public void setUp() throws Exception {		
//		// store inserted policy-key to cleanup
//		ContextManager ctxMan = initSampleContextManager();		
//		ContextStore ctxStore = ctxMan.getContextStore();
//		
//		// Check if the contexts are load properly
//		assertTrue(ctxStore.getContexts().size() == (NUM_ROOT_CONTEXTS + NUM_TENANT_CONTEXTS));
		setupRedisConfiguration();
		
		ctxSvc = new ContextSvcImpl(DOMAIN, REDIS_SERVER_ADDRESS);
		ctxSvc.init();
		
		// create sample context requests
		initSampleRequests();
	}

	private void setupRedisConfiguration() throws Exception {
		redisInsertedKeys = new ArrayList<String>();
		
		List<String> tenants = setupPolicies();
		
		setupTenantIdentifiers(tenants);		
	}

	private List<String> setupPolicies() throws Exception {
		ContextManager ctxMan = new ContextManager(DOMAIN, REDIS_SERVER_ADDRESS);

		Jedis jedis = new Jedis(REDIS_SERVER_ADDRESS);
		try {
			// Load provider policies to redis
			redisInsertedKeys.add(ctxMan.getProviderPolicyKey());
			jedis.set(ctxMan.getProviderPolicyKey(), PolicySetupUtil.loadPolicySet(TRANSFER_POLICY_FILE));
			
			
			// load inter-tenant policies to redis
			Map<String, String> tenantPolicies = PolicySetupUtil.loadPolicies(INTER_TENANT_POLICY_FILE);
			for(String pId : tenantPolicies.keySet()) {
				String pKey = ctxMan.getInterTenantPolicyKey(pId);
				redisInsertedKeys.add(pKey);
				
				jedis.set(pKey, tenantPolicies.get(pId));			
			}
			return new ArrayList<String>(tenantPolicies.keySet());
		}finally{
			jedis.disconnect();
		}
	}

	/**
	 * Add tenant ids to redis.
	 * 
	 * @param jedis
	 * @param tenants
	 */
	private void setupTenantIdentifiers(List<String> tenants) {
		Jedis jedis = new Jedis(REDIS_SERVER_ADDRESS);
		
		try {
			TenantManager tenantMgr = new TenantManager(DOMAIN, REDIS_SERVER_ADDRESS);
			StringBuilder builder = new StringBuilder();
			
			int index = 0;
			builder.append(tenants.get(index++));		
			for(; index < tenants.size(); index++) {
				builder.append(Configuration.TENANTID_DELIMITER + tenants.get(index));
			}
			
			jedis.set(tenantMgr.getTenantConfigKey(), builder.toString());
			redisInsertedKeys.add(tenantMgr.getTenantConfigKey());
		}finally{		
			jedis.disconnect();
		}
	}

	private void initSampleRequests() {
		String[][] requests = 	{
				{"http://demo3.uva.nl/vi/745/", "http://demo3.uva.nl/vi/745/ComputingNode", "SLI:Operate-VR:Stop"},
				{"http://demo3.uva.nl/vi/745/", "http://demo3.uva.nl/vi/745/StorageNode", "SLI:Remove-StorageImage"},
				{"http://demo3.uva.nl/vi/745/", "http://demo3.uva.nl/vi/745/NetworkNode3", "SLI:Monitor-VR-Power"},
				
				{"http://demo3.uva.nl/vi/438/", "http://demo3.uva.nl/vi/438/ComputingNode", "SLI:Subscribe-VR-Monitoring"},
				{"http://demo3.uva.nl/vi/185/", "http://demo3.uva.nl/vi/185/ComputingNode1", "SLI:Operate-VR:Checkpoint"},

				// inter-tenant requests				
				{"http://demo3.uva.nl/vi/438/", "http://demo3.uva.nl/vi/745/ComputingNode", "SLI:Monitor-VR-States"},
				{"http://demo3.uva.nl/vi/185/", "http://demo3.uva.nl/vi/438/ComputingNode", "SLI:Operate-VR:Restart"},				
				{"http://demo3.uva.nl/vi/478/", "http://demo3.uva.nl/vi/185//ResourcePool#IMFResourcePool/35", "SLI:Instantiate-VR"},				
		};
		
		this.ctxRequests = new ArrayList<ContextRequest>();
		
		for(int i = 0; i < requests.length; i++) {
			Map<String, String> subject = new HashMap<String, String>();
			subject.put(SUBJECT_ID, requests[i][0]);
			
			Map<String, String> permission = new HashMap<String, String>();
			permission.put(RESOURCE_ID, requests[i][1]);
			permission.put(ACTION_ID, requests[i][2]);
			
			ContextRequestImpl r = new ContextRequestImpl(subject, permission);	
			this.ctxRequests.add(r);
		}
	}

	@After
	public void tearDown() throws Exception {
		Jedis jedis = new Jedis(REDIS_SERVER_ADDRESS);
		for(String id: redisInsertedKeys) {
			jedis.del(id);
		}				
	}

//	@Test
	public void testValidate() {
		try {
			for(ContextRequest req : ctxRequests) {				
				ContextResponse resp = ctxSvc.validate(req);
				if (resp.getDecision() != ContextDecision.PERMIT || 
					resp.getDecision() != ContextDecision.NEED_REMOTE_DECISION){
					fail("Validating request failed:\n" + printRequest(req));
				}
			} 
		}catch(Exception | AssertionError e) {
			e.printStackTrace();
		}
	}

	private String printRequest(ContextRequest r) {
		StringBuffer buf = new StringBuffer();
				
		Map<String, String> attrs = r.getAttributes();
		for(String id: attrs.keySet()) {
			buf.append(id + ">>>" + attrs.get(id) + "\r\n");
		}
		return buf.toString();
	}
}
