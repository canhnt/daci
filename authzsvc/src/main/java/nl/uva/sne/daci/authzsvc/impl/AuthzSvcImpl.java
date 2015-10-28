package nl.uva.sne.daci.authzsvc.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import nl.uva.sne.daci.authzsvc.AuthzRequest;
import nl.uva.sne.daci.authzsvc.AuthzResponse;
import nl.uva.sne.daci.authzsvc.AuthzSvc;
import nl.uva.sne.daci.authzsvc.pdp.PDPSvcPool;
import nl.uva.sne.daci.authzsvc.pdp.PDPSvcPoolImpl;
import nl.uva.sne.daci.authzsvc.policy.PolicyManager;
import nl.uva.sne.daci.contextsvc.ContextSvc;
import nl.uva.sne.daci.contextsvc.tenant.TenantManager;

public class AuthzSvcImpl implements AuthzSvc {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthzSvcImpl.class);
	
	ContextHandler ctxHandler;
	
	PDPSvcPool servicePool;
	
	PolicyManager policyMgr;
	
	TenantManager tenantMgr;

	private ContextSvc contextsvc;
	
	public AuthzSvcImpl(ContextSvc ctxsvc) {
		
		if (ctxsvc == null) {
			log.error("Cannot connect to nl.uva.sne.daci.contextsvc.ContextSvc");
			throw new RuntimeException("Cannot connect to ContextSvc");
		}
		log.info("Connected to daci contextservice");
		this.contextsvc = ctxsvc;		
	}
	
	public void init(){
		log.info("Initializing DACI AuthzService");
		long startTime = System.currentTimeMillis();
		
		String authzPolicyKeyPrefix = String.format(Configuration.REDIS_KEYPREFIX_FORMAT, Configuration.DOMAIN);
		
		policyMgr = new PolicyManager(authzPolicyKeyPrefix, Configuration.REDIS_SERVER_ADDRESS);
		
		servicePool = new PDPSvcPoolImpl(policyMgr);
		
		tenantMgr = new TenantManager(Configuration.DOMAIN, Configuration.REDIS_SERVER_ADDRESS);
		try {
			tenantMgr.loadTenantIds();
		} catch (Exception e) {
			log.error("Error loading tenant identifiers");
			throw new RuntimeException("Error loading tenant identifiers", e);
		}
					
		// preload all tenants policies from server
		try {
			for(String tenantId : tenantMgr.getTenantIdentifiers()) {
				servicePool.getService(tenantId);
				log.debug("Loaded policies for tenant {}", tenantId);
			}			
		}catch(Exception e) {
			log.error("Error loading intra-tenants policies");
			throw new RuntimeException("Error loading intra-tenants policies", e);
		}
		log.info("Loaded policies for {} tenants", tenantMgr.getTenantIdentifiers().size());
		
		ctxHandler = new ContextHandler(this.contextsvc, servicePool, tenantMgr);
		
		long currentTime = System.currentTimeMillis();
		log.info("DACI AuthzService initialization done: {} ms", (currentTime-startTime));
	}
	
//	public void setContextService(ContextSvc contextSvc) {
//		this.contextsvc = contextSvc;
//	}
//	
//	public ContextSvc getContextService(){
//		return this.contextsvc;
//	}
	
//	public AuthzResponse authorize(AuthzRequest request) {
//		
//		return ctxHandler.process(request.getAttributes());
//	}
//
	
	public AuthzResponse authorize(String encodedTenantId, AuthzRequest request) {
		
		String tenantId;
		try {
			tenantId = URLDecoder.decode(encodedTenantId, "UTF-8");			
		} catch (UnsupportedEncodingException e1) {
			log.error("Error decoding tenantId{}", encodedTenantId);
			return new AuthzResponse(DecisionType.ERROR);
		}
		
//		log.info("Receive request for tenant {}", tenantId);
//		
//		for(String id : request.getAttributes().keySet()) {
//			log.info("{}:{}",id, request.getAttributes().get(id));
//		}

		// dummy processing:
//		return new AuthzResponse(DecisionType.PERMIT);
		
		try {
			long startTime = System.nanoTime();
			AuthzResponse response = ctxHandler.process(tenantId, request.getAttributes());

			long stopTime = System.nanoTime();
			log.info("Processing time: {} (ns);", (stopTime - startTime));
			
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return new AuthzResponse(DecisionType.ERROR);
		}
	}
}
