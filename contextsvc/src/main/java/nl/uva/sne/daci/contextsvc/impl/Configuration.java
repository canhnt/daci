package nl.uva.sne.daci.contextsvc.impl;

/**
 * 	urn:eu:geysers:daci:policy:xacml3:domain-id:inter-tenant:<trustor-id>"
 * @author canhnt
 *
 */
public class Configuration {

	public static final String PROVIDER_KEY_STYLE = "urn:eu:geysers:daci:%s:policy:xacml3:provider:root";
	
	public static final String INTERTENANT_KEY_STYLE = "urn:eu:geysers:daci:%s:policy:xacml3:inter-tenant:";

	public static final String DACI_TENANT_CONFIG_KEY = "urn:eu:geysers:daci:%s:config:tenants";
	
	public static final String TENANTID_DELIMITER = ";";
	
	public static final String REDIS_SERVER_ADDRESS = "localhost";

	public static final String DOMAIN = "daci-sne-demo";	
}
