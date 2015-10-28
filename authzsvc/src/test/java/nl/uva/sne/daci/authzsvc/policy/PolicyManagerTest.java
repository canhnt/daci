package nl.uva.sne.daci.authzsvc.policy;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;

import nl.uva.sne.daci.utils.XACMLUtil;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import redis.clients.jedis.Jedis;

public class PolicyManagerTest {

	private static final String SERVER_ADDRESS_TEST = "localhost";
	
	private static final String DOMAIN_TEST = "sne-testdomain";

	private static final String DOMAIN_KEY = String.format("urn:eu:geysers:daci:policy:xacml3:%s:intra-tenant", DOMAIN_TEST);

	private static final String SAMPLE_POLICY_FILE = "src/test/resources/vi-sample100-policy.xml";

	
	private List<String> keys;
	
	/**
	 *Add some sample policies to redis server 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
//	@Before
	public void addSamplePolicies() throws ParserConfigurationException, SAXException, IOException{
		
		Jedis jedis = new Jedis(SERVER_ADDRESS_TEST);
		try {
			Map<String, String> policies = loadPolicies();
			
			keys = new ArrayList<String>();
			
			// add policies to redis
			for(String psId: policies.keySet()) {
				
				// Key of policy to insert to the Redis server
				String psKey = DOMAIN_KEY + psId;
				
				keys.add(psKey); // save for future removal
				
				// add policy to redis server			
				jedis.set(psKey, policies.get(psId));
			}		
		}finally{
			jedis.disconnect();
		}
	}

	private Map<String, String> loadPolicies() throws ParserConfigurationException, SAXException, IOException {
		
		PolicySetType psRoot = XACMLUtil.unmarshalPolicySetType(SAMPLE_POLICY_FILE);
		
		// map of <policyId, policy_data>
		Map<String, String> policies = new HashMap<String, String>();
		for(JAXBElement<?> jaxbElement : psRoot.getPolicySetOrPolicyOrPolicySetIdReference()) {
			
			Object value = jaxbElement.getValue();
			
			if (value instanceof PolicySetType) {
				
				PolicySetType ps = (PolicySetType)value;				
				policies.put(ps.getPolicySetId(), XACMLUtil.marshal(ps));
			} else if (value instanceof PolicyType) {
				
				PolicyType p = (PolicyType)value;				
				policies.put(p.getPolicyId(), XACMLUtil.marshal(p));
			} else {
				System.err.println("Unknown object data type:" + value.toString());
			}
		}
		
		return policies;
	}

//	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException {
		PolicyManager pm = new PolicyManager(DOMAIN_KEY, SERVER_ADDRESS_TEST);
		
		for(String key: keys) {
			Object o =  pm.getPolicy(key);
			if (o instanceof PolicyType) {
				PolicyType p = (PolicyType)o;
				
				assertNotNull(p.getPolicyId());
				System.out.println("Retrieve policy from server: " + p.getPolicyId());
			} else if (o instanceof PolicySetType){
				PolicySetType ps = (PolicySetType) o;
				if (ps != null) {
					assertNotNull(ps.getPolicySetId());
					System.out.println("Retrieve policyset from server: " + ps.getPolicySetId());
				}  					
			} else {
				fail("Unknown policy type from server");
			}
		}		
	}

	/**
	 * Clean added sample policies in the test
	 */
//	@After 
	public void cleanSamplePolicies(){
		Jedis jedis = new Jedis(SERVER_ADDRESS_TEST);
		try{
			for(String key: keys) {
				jedis.del(key);
			}
		}finally{
			jedis.disconnect();
		}
	}
}
