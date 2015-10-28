package nl.uva.sne.daci.authzsvc.policy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import nl.uva.sne.daci.utils.XACMLUtil;
import nl.uva.sne.xacml.policy.finder.PolicyFinder;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;

import org.xml.sax.SAXException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class PolicyManager {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PolicyManager.class);
		
	JedisPool jedisPool;

	private String serverAddress;

	private String domainKeyRoot;
	
	public PolicyManager(String policyKeyPrefix, String redisServerAddress) {
		this.serverAddress = redisServerAddress;
		
		this.domainKeyRoot = policyKeyPrefix;
		
		jedisPool = new JedisPool(new JedisPoolConfig(), this.serverAddress);
	}

	public PolicyFinder createPolicyFinder(String tenantId) {
		
		String tenantKeyRoot = getTenantKeyRoot(tenantId);
//		System.out.println("Tenant key root: " + tenantKeyRoot);
		PolicyFinder pf = new TenantPolicyFinderImpl(tenantKeyRoot, this);
				
		return pf;
	}

	private String getTenantKeyRoot(String tenantId) {
		if (tenantId == null || tenantId.isEmpty())
			throw new RuntimeException("Empty tenant identifier");
		return domainKeyRoot + ":" + tenantId;
	}

	/**
	 * Return a PolicyType or a PolicySetType instance
	 * @param key
	 * @return
	 */
	public Object getPolicy(String key) {
		log.debug("Get policy: " + key);
		Jedis jedis = jedisPool.getResource();
		
		try{
			String v = jedis.get(key);
			
			InputStream is = new ByteArrayInputStream(v.getBytes());
			
			try {
				PolicyType p;
				p = XACMLUtil.unmarshalPolicyType(is);
				if (p != null)
					return p;
				
				PolicySetType ps = XACMLUtil.unmarshalPolicySetType(is);
				if (ps != null)
					return ps;				
			} catch (ParserConfigurationException | SAXException | IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			
			throw new RuntimeException("Unable to marshal policy:" + v);
			
		}finally {
			jedisPool.returnResource(jedis);
		}		
	}
}
