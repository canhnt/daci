package nl.uva.sne.daci.tenant.trustadmin;

import java.util.List;


/**
 * Service to manage trusts with other tenants via trust policies (e.g. XACML) 
 * @author canhnt
 *
 */
public interface TrustAdminSvc {
	
	/**
	 * Set the trust with the target tenant 'trustee'. The trust context is defined by the policy documentation.
	 * @param trusteeId
	 * @param policydoc
	 */
	public void setTrustPolicy(String trusteeId, String policydoc);
	
	public String getTrustPolicy(String trusteeId, String policydoc);
	
	public List<String> listTrustPolicies();
}
