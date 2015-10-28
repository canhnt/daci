package nl.uva.sne.daci.authzsvc.pdp;

import java.util.HashMap;
import java.util.Map;

import nl.uva.sne.daci.authzsvc.policy.PolicyManager;
import nl.uva.sne.xacml.policy.finder.PolicyFinder;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;

public class PDPSvcPoolImpl implements PDPSvcPool {

    private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PDPSvcPoolImpl.class);
	
    private Map<String, PDPSvc> servicePool = null;

    private PolicyManager policyManager;
    
    public PDPSvcPoolImpl(PolicyManager policyManager) {
    	this.policyManager = policyManager;
    	
    	servicePool = new HashMap<String, PDPSvc>();    
    }
    
	@Override
	public PDPSvc getService(String id) throws Exception {
		if (servicePool.containsKey(id))
			return servicePool.get(id);
		else {			
			try {
				PDPSvc newPDP = createPDPSvc(id);
				servicePool.put(id, newPDP);
				return newPDP;				
			} catch (Exception e) {
				log.error("Error creating PDP service for tenant {}", id);
				throw e;
			}			
		}		
	}

	/**
	 * Create a new PDP service by loading equivalent tenant's policies 
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	private PDPSvc createPDPSvc(String id) throws Exception {
		PolicyFinder policyFinder = this.policyManager.createPolicyFinder(id);
		
		// get policy root, beware! here
		Object policyObj = policyFinder.lookup(null);
		
		if (policyObj instanceof PolicySetType)
			return new PDPSvcImpl((PolicySetType)policyObj, policyFinder);
		else if (policyObj instanceof PolicyType) 
			return new PDPSvcImpl((PolicyType)policyObj, policyFinder);
		else
			throw new Exception("Unknown policy type for given id:" + id);
		
	}

}
