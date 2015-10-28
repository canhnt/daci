package nl.uva.sne.daci.context.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;

import nl.uva.sne.daci.context.Context;
import nl.uva.sne.daci.context.ContextStore;
import nl.uva.sne.daci.context.PolicySetupUtil;
import nl.uva.sne.daci.utils.XACMLUtil;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import redis.clients.jedis.Jedis;

public class ContextManagerTest {
	public static final String REDIS_SERVER_ADDRESS = "localhost";
	public static final String DOMAIN = "demo3-sne";
	
	private static final String TRANSFER_POLICY_FILE = "src/test/resources/transfer-policies.xml";
	private static final int NUM_ROOT_CONTEXTS = 3;
	
	private static final String INTER_TENANT_POLICY_FILE = "src/test/resources/inter-tenant-policies.xml";
	private static final int NUM_TENANT_CONTEXTS = 4;
	
	ContextManager ctxMan;
	
	List<String> policyKeys;
	
	@Before
	public void setUpBeforeClass() throws Exception {
		// store inserted policy-key to cleanup
		policyKeys = new ArrayList<String>();
		
		ctxMan = new ContextManager(DOMAIN, REDIS_SERVER_ADDRESS);		
		
		Jedis jedis = new Jedis(REDIS_SERVER_ADDRESS);
		
		// Load provider policies.
		String psString = PolicySetupUtil.loadPolicySet(TRANSFER_POLICY_FILE);						
		policyKeys.add(ctxMan.getProviderPolicyKey());
		jedis.set(ctxMan.getProviderPolicyKey(), psString);
				
		// load inter-tenant policies
		Map<String, String> tenantPolicies = PolicySetupUtil.loadPolicies(INTER_TENANT_POLICY_FILE);
		for(String pId : tenantPolicies.keySet()) {
			String pKey = ctxMan.getInterTenantPolicyKey(pId);
			policyKeys.add(pKey);
			
			jedis.set(pKey, tenantPolicies.get(pId));			
		}
		
		List<String> tenants = new ArrayList<String>(tenantPolicies.keySet());		
		ctxMan.setTenantIdentifiers(tenants);
	}
	
	@After
	public void tearDownAfterClass() throws Exception {
		Jedis jedis = new Jedis(REDIS_SERVER_ADDRESS);
		for(String id: policyKeys) {
			jedis.del(id);
		}		
	}

	@Test
	public void testLoadProviderContexts() throws Exception {
		this.ctxMan.loadProviderContexts();
		ContextStore ctxStore = ctxMan.getContextStore();
		for(Context c: ctxStore.getContexts()) {
			assertTrue(ctxStore.isRootContext(c));
		}
		assertTrue(ctxStore.getContexts().size() == NUM_ROOT_CONTEXTS);
	}

	@Test
	public void testLoadInterTenantContexts() throws Exception {
		
		ctxMan.loadInterTenantContexts();
		ContextStore ctxStore = ctxMan.getContextStore();
		
		assertTrue(ctxStore.getContexts().size() == NUM_TENANT_CONTEXTS);
	}

}
