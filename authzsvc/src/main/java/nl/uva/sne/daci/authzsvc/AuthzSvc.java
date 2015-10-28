package nl.uva.sne.daci.authzsvc;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


public interface AuthzSvc {

	public static final String AUTHZREQUEST = "daci_authzrequest";
	
	public enum DecisionType{
		PERMIT,
		DENY,
		NOT_APPLICABLE,
		ERROR
	}
	
	@POST
	@Path("{tenantId}/authorize")
    @Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public AuthzResponse authorize (@PathParam("tenantId") String tenantId, AuthzRequest request);	
}
