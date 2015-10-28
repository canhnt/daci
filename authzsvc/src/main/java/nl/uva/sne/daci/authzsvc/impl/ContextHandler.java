package nl.uva.sne.daci.authzsvc.impl;

import java.util.List;
import java.util.Map;

import nl.uva.sne.daci.authzsvc.AuthzResponse;
import nl.uva.sne.daci.authzsvc.AuthzSvc;
import nl.uva.sne.daci.authzsvc.AuthzSvc.DecisionType;
import nl.uva.sne.daci.authzsvc.pdp.PDPSvc;
import nl.uva.sne.daci.authzsvc.pdp.PDPSvcPool;
import nl.uva.sne.daci.context.ContextRequest;
import nl.uva.sne.daci.context.ContextRequestBuilder;
import nl.uva.sne.daci.context.ContextResponse;
import nl.uva.sne.daci.contextsvc.ContextSvc;
import nl.uva.sne.daci.contextsvc.tenant.TenantManager;
import nl.uva.sne.daci.utils.XACMLUtil;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;

/**
 * Handling authorization request:
 *  - Get tenant-id
 *  - Send request to the PDP[tenantId]. 
 *  - If permit, obtain authzCtx object from the request.
 *  - Send authzCtx to Trust-Resolution-Service to validate the trust.
 *  - Response: 
 *  	+ If existing a trust path from authzCtx to root of trust dlgCtx:
 *  		# if it's the remote resource (of other provider):
 *  			perform remote trust establishment protocol: receive trust-credential or failed response.
 *  		# if it's the local resource: return the authzToken issued by local TokenAuthority.
 *  	+ Otherwise, return deny.
 * @author canhnt
 *
 */
public class ContextHandler {
	
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContextHandler.class);
	
//	private static final String URN_ATTRIBUTE_TENANT_ID = "urn:eu:geysers:daci:tenant:tenant-id";
			
	PDPSvcPool servicePool = null;

	private XACMLUtil xacmlUtil;

	private ContextSvc contextSvc;

	private TenantManager tenantMgr;

	private List<String> tenantIds;
	
	public ContextHandler(ContextSvc ctxsvc, PDPSvcPool servicePool, TenantManager tenantMgr) {
		if (ctxsvc == null || servicePool == null || tenantMgr == null)
			throw new RuntimeException("Null arguments to construct ContextHandler object");
		
		this.contextSvc = ctxsvc;
		this.servicePool = servicePool;
		this.tenantMgr = tenantMgr;
		
		this.tenantIds = tenantMgr.getTenantIdentifiers();
		
		if (tenantIds == null || this.tenantIds.size() == 0)
			throw new RuntimeException("Error loading tenant identifiers from server");	
		
		this.xacmlUtil = new XACMLUtil();		
	}
	
	public AuthzResponse process(String tenantId, Map<String, String> request) throws Exception{
		
//		String tenantId = request.get(URN_ATTRIBUTE_TENANT_ID);
		if (tenantId == null || tenantId.isEmpty())
			throw new RuntimeException("Invalid request without tenant-id attribute");
		
		if (!this.tenantIds.contains(tenantId)) {
			log.error("Tenant {} does not exist in the system", tenantId);
			return new AuthzResponse(DecisionType.ERROR);
		}
		
		// Retrieve the PDP of the tenant-id
		PDPSvc pdp;
		try {
			pdp = servicePool.getService(tenantId);
		} catch (Exception e) {
			log.error("PDP of the tenant {} not found", tenantId);
			return createErrorResponse();
		} 
		
		if (pdp != null)
			log.debug("Authz at PDP of tenant {}", tenantId);
		
		// Create a XACML request format
		RequestType xacmlRequest = xacmlUtil.createRequest(request);
				
		ResponseType xacmlResponse = pdp.evaluate(xacmlRequest);
		DecisionType pdpDecision = ContextHandler.getDecision(xacmlResponse);
		
		// if tenant's PDP permits, create an ContextRequest and validate at the Context Service
		log.debug("PDP decision: {}", pdpDecision);
		
		if (pdpDecision == AuthzSvc.DecisionType.PERMIT) {
			ContextRequest ctxRequest = createContextRequest(tenantId, request);			
			
			return validateContext(ctxRequest);
		}
		else {
			log.debug("Decision from ctxservice:{}", pdpDecision);
			return new AuthzResponse(pdpDecision);
		}
	}

	private AuthzResponse createErrorResponse() {
		AuthzResponse r = new AuthzResponse(DecisionType.ERROR);		
		return r;
	}

	/**
	 * Send authzCtx to Context-Service to validate the trust.
	 * 
	 * @param ctxRequest
	 * @return Return the token issued by local or remote TokenAuthority if it's success
	 * Otherwise null.
	 * @throws Exception 
	 */
	private AuthzResponse validateContext(ContextRequest ctxRequest) throws Exception {	
		
		ContextResponse resp = this.contextSvc.validate(ctxRequest);
		
		log.debug("Receive response from contextsvc:{}", resp.getDecision());
		
		switch(resp.getDecision()) {
		case DENY:
			return new AuthzResponse(DecisionType.DENY);
			
		case ERROR:
			return new AuthzResponse(DecisionType.ERROR);
			
		case NEED_REMOTE_DECISION:
			return new AuthzResponse(DecisionType.PERMIT, resp.getGrantToken());
			
		case PERMIT:
			return new AuthzResponse(DecisionType.PERMIT);
			
		default:
			log.error("Unknown decision from contextsvc:{}", resp.getDecision());
			log.debug("Unknown decision from contextsvc for given request: {}", ctxRequest.toString());
			return new AuthzResponse(DecisionType.ERROR);
		}		
	}

	private ContextRequest createContextRequest(String tenantId, Map<String, String> request) {

		// Replace all original subject attributes with tenant's attributes		
		ContextRequest ctxRequest = ContextRequestBuilder.create(tenantId, request);
				
		log.debug("Sending request to contextsvc: {}", ctxRequest.toString());
		return ctxRequest;		
	}
	
	/**
	 * Mapping from XACML Response to @nl.uva.sne.daci.authzsvc.AuthzSvc.DecisionType
	 * 
	 * @param r
	 * @return
	 */
	public static DecisionType getDecision(ResponseType r) {
		if (r.getResult() != null && r.getResult().size() != 0) {
			ResultType result = r.getResult().get(0);
			switch(result.getDecision()){
			case DENY:
				return DecisionType.DENY;
			case INDETERMINATE:
				return DecisionType.ERROR;
			case NOT_APPLICABLE:
				return DecisionType.NOT_APPLICABLE;
			case PERMIT:
				return DecisionType.PERMIT;
			}
		}
		log.error("XACML PDP return null or empty decision");
		return DecisionType.ERROR;
	}	
}
