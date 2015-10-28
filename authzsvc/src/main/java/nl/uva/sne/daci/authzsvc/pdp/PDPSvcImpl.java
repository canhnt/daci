package nl.uva.sne.daci.authzsvc.pdp;

import nl.uva.sne.midd.MIDDException;
import nl.uva.sne.xacml.PDP;
import nl.uva.sne.xacml.policy.finder.PolicyFinder;
import nl.uva.sne.xacml.policy.parsers.XACMLParsingException;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;

public class PDPSvcImpl implements PDPSvc {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PDPSvcImpl.class);
	
	PDP pdp;
	
	@Override
	public ResponseType evaluate(RequestType xacmlRequest) {
		
		return pdp.evaluate(xacmlRequest);
	}

	
	public PDPSvcImpl(PolicySetType policyset, PolicyFinder policyFinder) throws Exception {
		try {
			pdp = new PDP(policyset, policyFinder);
			pdp.initialize();
		} catch (XACMLParsingException | MIDDException e) {
			log.error("Initialize PDP error");
			throw e;
		} 						
	}


	public PDPSvcImpl(PolicyType policy, PolicyFinder policyFinder) throws Exception {		
		try {
			pdp = new PDP(policy);
			pdp.initialize();
		} catch (XACMLParsingException | MIDDException e) {
			log.error("Initialize PDP error");
			throw e;
		} 
	}
}
