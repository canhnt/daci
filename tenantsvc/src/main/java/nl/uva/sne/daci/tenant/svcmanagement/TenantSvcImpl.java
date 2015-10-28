/**
 * 
 */
package nl.uva.sne.daci.tenant.svcmanagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author canhnt
 *
 */
public class TenantSvcImpl implements TenantSvc {

	private static final Logger logger = LoggerFactory.getLogger(TenantSvcImpl.class);

	/* (non-Javadoc)
	 * @see nl.uva.sne.daci.tenantmanager.TenantSvc#reserve(java.lang.String)
	 */
	@Override
	public String createTenant(String tenantId) {
		logger.info("Reserve a new tenant with Id: {}", tenantId);	
		// - Add a new entry in tenant-table: tenant-id | vi-description | tenant-pubkey
		return null;
	}

	/* (non-Javadoc)
	 * @see nl.uva.sne.daci.tenantmanager.TenantSvc#instantiate(java.lang.String)
	 */
	@Override
	public int instantiateTenant(String tenantId) {
		logger.info("Instantiate the tenant with Id: {}", tenantId);
		
		/*
		 * - Generate delegation policy: DelegationPolicyManager:generatePolicy(tenantId, viDescription)
		 * - Add a new row in Tenant Authz DB:  |tenant-id| tenant-policies  (empty)| 
		 */
				
		return 0;
	}

	/* (non-Javadoc)
	 * @see nl.uva.sne.daci.tenantmanager.TenantSvc#decommission(java.lang.String)
	 */
	@Override
	public int removeTenant(String tenantId) {
		logger.info("Decommission the tenant with Id: {}", tenantId);
		/**
		 * - Remove tenant's delegation policies in DelegationPoliciesDB
		 * - Remove tenant's authz policies in Tenant Authz DB
		 * - Remove tenant's record in TenantDB
		 */
		return 0;
	}

}
