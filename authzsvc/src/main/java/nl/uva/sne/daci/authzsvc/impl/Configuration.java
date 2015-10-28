package nl.uva.sne.daci.authzsvc.impl;

/**
 * 	urn:eu:geysers:daci:policy:xacml3:domain-id:inter-tenant:<trustor-id>"
 * @author canhnt
 *
 */
public class Configuration {
	/**
	 * root key of all policies in REDIS server.
	 * key scheme:
	 * 		urn:eu:geysers:daci:policy:xacml3:<domain>:intra-tenant:<tenant-id>:<policy-id>
	 */	
	public static final String REDIS_KEYPREFIX_FORMAT = "urn:eu:geysers:daci:%s:policy:xacml3:intra-tenant";
	
	public static final String REDIS_SERVER_ADDRESS = "localhost";

	public static final String DOMAIN = "daci-sne-demo";

	
}
