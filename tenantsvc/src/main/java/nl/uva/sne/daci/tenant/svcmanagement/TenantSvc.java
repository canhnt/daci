package nl.uva.sne.daci.tenant.svcmanagement;

public interface TenantSvc {
	
	public String createTenant(String tenantId);
	
	public int instantiateTenant(String tenantId);
	
	public int removeTenant(String tenantId);
}
