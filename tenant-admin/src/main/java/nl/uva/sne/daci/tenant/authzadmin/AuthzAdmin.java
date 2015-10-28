package nl.uva.sne.daci.tenant.authzadmin;

import java.util.List;

public interface AuthzAdmin {
	
	public void putPolicy(String policyId, String policydoc);
	
	public void deletePolicy(String policyId);
	
	public String getPolicy(String policyId);
	
	public List<String> listPolicies();
}
