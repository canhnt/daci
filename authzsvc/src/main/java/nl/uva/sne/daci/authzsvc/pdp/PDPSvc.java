package nl.uva.sne.daci.authzsvc.pdp;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;

public interface PDPSvc {
	
	public ResponseType evaluate(RequestType xacmlRequest);
}
