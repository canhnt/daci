package nl.uva.sne.daci.context.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import nl.uva.sne.daci.context.Context;
import nl.uva.sne.daci.context.ContextStore;
import nl.uva.sne.daci.contextsvc.impl.Configuration;
import nl.uva.sne.daci.contextsvc.impl.ContextSvcImpl;
import nl.uva.sne.daci.contextsvc.tenant.TenantManager;
import nl.uva.sne.midd.nodes.AbstractNode;
import nl.uva.sne.midd.nodes.InternalNode;
import nl.uva.sne.xacml.AttributeMapper;
import nl.uva.sne.xacml.policy.parsers.PolicyParser;
import nl.uva.sne.xacml.util.XACMLUtil;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import redis.clients.jedis.Jedis;

/**
 * Load inter-tenant and provider policies from Redis server
 */
public class ContextManager {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContextManager.class);

	private String serverAddress;
	
	protected String providerPolicyKey;

	private String interTenantKeyPrefix;

	private ContextStoreImpl ctxStore;

	private AttributeMapper attrMapper;

	private List<String> tenantIds;

	public ContextManager(String domain, String redisServerAddress) {
		this.providerPolicyKey = String.format(Configuration.PROVIDER_KEY_STYLE, domain);
		this.interTenantKeyPrefix = String.format(Configuration.INTERTENANT_KEY_STYLE, domain);
		
		this.serverAddress = redisServerAddress;	
		
		ctxStore = new ContextStoreImpl();
		
		attrMapper = new AttributeMapper();
	}

	public ContextStore getContextStore(){
		return ctxStore;
	}
	
	public String getProviderPolicyKey() {
		return this.providerPolicyKey;
	}
	/**
	 * Load provider-tenant delegation policies and convert to contexts.
	 * They are the root contexts by default (in single-domain use-cases)
	 *  
	 * @throws Exception
	 */
	public void loadProviderContexts() throws Exception {
		Jedis jedis = new Jedis(serverAddress);
		
		try {
			Map<String, String> issuerAttrs  = getProviderIssuer();
			
			/* The provider policy-key points to a policyset containing children PolicyType objects,
			 * each represents a transferred policy from provider to a tenant.
			 * */
			List<PolicyType> policies = loadPolicies(jedis, this.providerPolicyKey);
			if (policies == null || policies.size() == 0) {
				throw new RuntimeException("Cannot load provider policies from server:" + this.providerPolicyKey);
			}				
			
			for(PolicyType p: policies) {
				Context c = createContext(issuerAttrs, p);			
				ctxStore.addRoot(c);
			}
		}finally{
			jedis.disconnect();
		}
	}
	
	/**
	 * Load a Policyset from Redis DB and return list of children Policy.
	 * 
	 * @param key
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private List<PolicyType> loadPolicies(Jedis jedis, String key) throws Exception {
		PolicySetType ps = XACMLUtil.unmarshalPolicySetType(new ByteArrayInputStream(jedis.get(key).getBytes()));
		
		if (ps != null && ps.getPolicySetId() != null) { 
			List<PolicyType> policies = new ArrayList<PolicyType>();
			
			//get all children policies
			for(JAXBElement<?> obj: ps.getPolicySetOrPolicyOrPolicySetIdReference()) {
				Object objValue = obj.getValue();
				if (objValue instanceof PolicyType) {
					policies.add((PolicyType) objValue);
				}
			}
			if (policies.size() > 0) 
				return policies;
		}		
		throw new RuntimeException("Error loading provider policies at:" + key);
	}
	
	private Map<String, String> getProviderIssuer() {
		Map<String, String> subject = new HashMap<String, String>();
		subject.put("urn:oasis:names:tc:xacml:1.0:subject:subject-id", "provider");
		return subject;
	}

	/**
	 * Load inter-tenant policies and convert to contexts
	 * @throws Exception
	 */
	public void loadInterTenantContexts() throws Exception {
		Jedis jedis = new Jedis(serverAddress);
		
		try{			
			List<String> tenants = getTenantIdentifiers();
			
			if (tenants == null || tenants.size() == 0){
				throw new Exception("No tenant identifiers found");
			}
			
			log.debug("Number of tenants:{}", tenants.size());
			
			for(String t : tenants) {
				String key = getInterTenantPolicyKey(t);
				PolicyType p = loadPolicy(jedis, key);
				
				Map<String, String> issuerAttrs  = createTenantIssuer(t); 
				Context c = createContext(issuerAttrs, p);
				log.debug("Load contexts for tenant {}", t);
				ctxStore.add(c);			
			}			
		}finally{
			jedis.disconnect();
		}
	}
	
	/**
	 * Construct the policy key identifier in Redis from the trustor-id. 
	 * @param trustor
	 * @return
	 */
	public String getInterTenantPolicyKey(String trustor) {
		if (trustor == null || trustor.isEmpty())
			throw new RuntimeException("Empty trustor tenant identifier");
		return interTenantKeyPrefix + trustor;
	}

	/**
	 * Create a context representing the issuer asserts a criteria inside the policy p.
	 *  
	 * @param issuerAttrs
	 * @param p
	 * @return
	 * @throws Exception
	 */
	private Context createContext(Map<String, String> issuerAttrs, PolicyType p) throws Exception {
		if (issuerAttrs == null || issuerAttrs.size() == 0)
			throw new IllegalArgumentException("Issuer arugment must not be null");
		
		if (p == null || p.getPolicyId() == null)
			throw new IllegalArgumentException("Policy arugment must not be null");
		
		PolicyParser parser = new PolicyParser(null, p, attrMapper);		
		AbstractNode n = parser.parse();
		
		if (n instanceof InternalNode) {
			Context ctx = new ContextImpl(issuerAttrs, (InternalNode)n, attrMapper);
			return ctx;
		}
		
		throw new Exception("Parsing policy error:" + p.getPolicyId());
	}

	/**
	 * Create the vector of attributes representing a tenant.
	 * @param t
	 * @return
	 */
	private Map<String, String> createTenantIssuer(String t) {
		Map<String, String> subject = new HashMap<String, String>();
		subject.put("urn:oasis:names:tc:xacml:1.0:subject:subject-id", t);
		return subject;
	}

	// retrieve tenants from DB
	private List<String> getTenantIdentifiers() {
		return tenantIds;
	}

	/**
	 * Load a policy from Redis DB 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private PolicyType loadPolicy(Jedis jedis, String policyKeyId) throws Exception {
		if (jedis == null || policyKeyId == null || policyKeyId.isEmpty())
			throw new NullPointerException("Null parameters for the method");
		
		log.debug("Load policy " + policyKeyId + " from Redis server");
		String policyStr = jedis.get(policyKeyId);
		
		PolicyType p = XACMLUtil.unmarshalPolicyType(new ByteArrayInputStream(policyStr.getBytes()));
			
		if (p != null && p.getPolicyId() != null) {
			return p;
		}  								
		throw new RuntimeException("Error loading policy:" + policyKeyId);
	}

	public void setTenantIdentifiers(List<String> tenants) {
		this.tenantIds = tenants;
		
	}
}
