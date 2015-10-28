package nl.uva.sne.daci.setup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import nl.uva.sne.daci.context.impl.ContextManager;
import nl.uva.sne.daci.contextsvc.impl.Configuration;
import nl.uva.sne.daci.contextsvc.tenant.TenantManager;
import redis.clients.jedis.Jedis;

public class PolicySetup {
	public static final String REDIS_SERVER_ADDRESS = "localhost";
//	public static final String DOMAIN = "demo3-sne";
	public static final String DOMAIN = "daci-sne-demo";
	
	private static final String TRANSFER_POLICY_FILE = "policies/vi-sample1k-provider-policies.xml";
	
	private static final String INTER_TENANT_POLICY_FILE = "policies/vi-sample1k-intertenant-policies.xml";
	
	private static final String INTRA_TENANT_POLICY_FILE = "policies/vi-sample1k-intratenant-policies.xml";
	
	ContextManager ctxMan;
	
	List<String> redisInsertedKeys;
	
	private String transferPolicyFile;
	
	private String interTenantPolicyFile;
	
	private String redisServerAddress;
	
	private String domain;
	private String intraTenantPolicyFile;
	
	public PolicySetup(String domain, String redisServerAddress, 
			String transferPolicyFile, 
			String interTenantPolicyFile,
			String intraTenantPolicyFile) {
		this.domain = domain;		
		this.redisServerAddress = redisServerAddress;
		
		this.transferPolicyFile 	= transferPolicyFile;
		this.interTenantPolicyFile 	= interTenantPolicyFile;
		this.intraTenantPolicyFile	= intraTenantPolicyFile;
	}

	public static void main(String[] args) throws Exception {

		String domain = null;//DOMAIN;
		String redisServer = null;//REDIS_SERVER_ADDRESS;
		
		String transferPolicyFile = null;//TRANSFER_POLICY_FILE;
		String interTenantPolicyFile = null;//INTER_TENANT_POLICY_FILE;
		String intraTenantPolicyFile = null;//INTRA_TENANT_POLICY_FILE;

		if (args.length == 5) {
			domain = args[0];
			redisServer = args[1];
			
			transferPolicyFile = args[2];
			interTenantPolicyFile = args[3];
			intraTenantPolicyFile = args[4];
		} else {
			System.out.println("policysetup <domain> <redis-server-address> <provider-policy-file> <inter-tenant-policy-file> <intra-tenant-policy-file>");
			return;
		}
		
//		String s = "http://demo3.uva.nl/vi/745/;http://demo3.uva.nl/vi/438/;http://demo3.uva.nl/vi/185/;http://demo3.uva.nl/vi/478/";
//		String[] list = s.split(";");
//		for(int i = 0; i < list.length; i++) {
//			System.out.println(list[i]);
//		}
		System.out.println("Setup policies to Redis server:" + redisServer + " with domain:" + domain);
		
		System.out.println("transferPolicyFile:" + transferPolicyFile);
		System.out.println("interTenantPolicyFile:" + interTenantPolicyFile);
		System.out.println("intraTenantPolicyFile:" + intraTenantPolicyFile);
				
		PolicySetup setup = new PolicySetup(domain, redisServer, 
				transferPolicyFile, 
				interTenantPolicyFile,
				intraTenantPolicyFile);
		
		List<String> tenants = setup.setupPolicies();
		
		setup.setupTenantIdentifiers(tenants);
		
		System.out.println("Done");
	}
	
	/**
	 * Load policies from files and add to Redis server. 
	 * 
	 * @return The list of tenant identifiers for loaded policies.
	 * @throws Exception
	 */
	private List<String> setupPolicies() throws Exception {
		System.out.println("Load policies of domain " + this.domain + " to Redis server " + this.redisServerAddress);
		
		// store inserted policy-key to cleanup
		redisInsertedKeys = new ArrayList<String>();
		
		ctxMan = new ContextManager(this.domain, this.redisServerAddress);		
		
		Jedis jedis = new Jedis(this.redisServerAddress);
		
		// Load provider policies.						
		setupProviderPolicies(jedis);
		
		// load inter-tenant policies
		Map<String, String> tenantPolicies = setupInterTenantPolicies(jedis);
		
		// setup intra-tenant policies
		setupIntraTenantPolicies(jedis);
		
		jedis.disconnect();
		
		return new ArrayList<String>(tenantPolicies.keySet());
	}
	
	private void setupIntraTenantPolicies(Jedis jedis) throws Exception {
		
		Map<String, String> tenantPolicies = PolicySetupUtil.loadPolicies(this.intraTenantPolicyFile);
		
		String authzPolicyKeyPrefix = String.format(nl.uva.sne.daci.authzsvc.impl.Configuration.REDIS_KEYPREFIX_FORMAT, DOMAIN);
						
		for(String pId : tenantPolicies.keySet()) {
			String pKey = authzPolicyKeyPrefix + ":" + pId;
			redisInsertedKeys.add(pKey);			
			jedis.set(pKey, tenantPolicies.get(pId));
			System.out.println("Put intra-tenant policy:" + pKey);
		}
		System.out.println("Put " + tenantPolicies.size() + " intra-tenant policies");		
	}

	private Map<String, String> setupInterTenantPolicies(Jedis jedis)
			throws ParserConfigurationException, SAXException, IOException {
		
		Map<String, String> tenantPolicies = PolicySetupUtil.loadPolicies(this.interTenantPolicyFile);
		for(String pId : tenantPolicies.keySet()) {
			String pKey = ctxMan.getInterTenantPolicyKey(pId);
			redisInsertedKeys.add(pKey);			
			jedis.set(pKey, tenantPolicies.get(pId));
			
			System.out.println("Put inter-tenant policy:" + pKey);
		}
		System.out.println("Put " + tenantPolicies.size() + " inter-tenant policies");
		return tenantPolicies;
	}

	private void setupProviderPolicies(Jedis jedis) throws Exception {
		redisInsertedKeys.add(ctxMan.getProviderPolicyKey());
		jedis.set(ctxMan.getProviderPolicyKey(), PolicySetupUtil.loadPolicySet(this.transferPolicyFile));
		System.out.println("Put provider policies to " + ctxMan.getProviderPolicyKey());
	}

	private void setupTenantIdentifiers(List<String> tenants) {
		Jedis jedis = new Jedis(this.redisServerAddress);
		
		TenantManager tenantMgr = new TenantManager(this.domain, this.redisServerAddress);
		StringBuilder builder = new StringBuilder();
		
		int index = 0;
		builder.append(tenants.get(index++));		
		for(; index < tenants.size(); index++) {
			builder.append(Configuration.TENANTID_DELIMITER + tenants.get(index));
		}
				
		jedis.set(tenantMgr.getTenantConfigKey(), builder.toString());
		redisInsertedKeys.add(tenantMgr.getTenantConfigKey());
		
		System.out.println("Added tenant identifiers to " + tenantMgr.getTenantConfigKey());
		
		jedis.disconnect();
	}
}
