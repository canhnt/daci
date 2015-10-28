package nl.uva.sne.daci.tokensvc.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


public interface RESTTokenSvc {
	
	String TENANT_ID = "daci_tenant_id";
	String REQUEST = "daci_request";
	String KEYINFO = "daci_keyinfo";
	String TOKEN = "daci_token";

	@POST
	@Path("/grant_token")
	@Produces({"application/xml","application/json"})
	@Consumes({"application/xml","application/json","application/x-www-form-urlencoded"})
	Response issueGrantToken(@FormParam(TENANT_ID) String tenantId, 
							 @FormParam(REQUEST) String request, 
							 @FormParam(KEYINFO) String userKeyInfo);
	
	@POST
	@Path("/verify_grant_token")
	@Consumes({"application/xml","application/json","application/x-www-form-urlencoded"})
	@Produces({"application/xml","application/json"})
	boolean verifyGrantToken(@FormParam(TOKEN) String token);
}
