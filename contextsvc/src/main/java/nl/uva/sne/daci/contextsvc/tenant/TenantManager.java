package nl.uva.sne.daci.contextsvc.tenant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nl.uva.sne.daci.contextsvc.impl.Configuration;
import nl.uva.sne.daci.contextsvc.impl.ContextSvcImpl;
import redis.clients.jedis.Jedis;

public class TenantManager {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TenantManager.class);
	
	private String serverAddress;
	
	private List<String> tenantIds;
	
	// Key to the list of tenant identifiers stored in the Redis server
	private String tenantListConfigKey;
	
	public TenantManager(String domain, String redisServerAddress){
		
		this.serverAddress = redisServerAddress;
		this.tenantListConfigKey = String.format(Configuration.DACI_TENANT_CONFIG_KEY, domain);
	}
	
	public String getTenantConfigKey(){
		return this.tenantListConfigKey;
	}
	
	public void loadTenantIds() throws Exception{
		Jedis jedis = new Jedis(this.serverAddress);
		
		try {
			String strTenants = jedis.get(tenantListConfigKey);
			log.debug("Loaded tenants string:" + strTenants);
			
			String ids[] = strTenants.split(Configuration.TENANTID_DELIMITER);
			if (ids != null && ids.length > 0){
				this.tenantIds = new ArrayList<String>(Arrays.asList(ids));
			} else {			
				throw new Exception("Error: loaded tenants from Redis server, but failed to parse:" + strTenants);			
			}					
		}finally{		
			jedis.disconnect();
		}
	}
	
	public List<String> getTenantIdentifiers(){
		if (tenantIds == null || tenantIds.size() == 0)
			throw new RuntimeException("Error loading tenant identifiers: null or empty list of tenants");
		
		return tenantIds;
	} 
}
