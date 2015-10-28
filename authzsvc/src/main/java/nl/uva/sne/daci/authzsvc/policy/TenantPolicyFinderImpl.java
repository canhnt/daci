package nl.uva.sne.daci.authzsvc.policy;

import nl.uva.sne.xacml.policy.finder.PolicyFinder;

public class TenantPolicyFinderImpl implements PolicyFinder {

	private String keyPrefix = null;
	
	private PolicyManager policyMan;

	protected TenantPolicyFinderImpl(String keyPrefix, PolicyManager policyMan) {		
		
		this.keyPrefix = keyPrefix;
		this.policyMan = policyMan;
		
	}

	@Override
	public Object lookup(String policyId) {
		String key ;
		// if policyid is null, then lookup the root
		if (policyId == null || policyId.isEmpty())
			key = this.keyPrefix;
		else
			key = this.keyPrefix + ":" + policyId;
		
		return policyMan.getPolicy(key);		
	}

}
