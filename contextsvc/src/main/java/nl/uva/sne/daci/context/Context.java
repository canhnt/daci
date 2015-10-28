package nl.uva.sne.daci.context;

import java.util.Map;

public interface Context {
	/**
	 * Return list of attributes representing the context issuer.
	 * @return
	 */
	Map<String, String> getIssuerAttributes();
	
	/**
	 * Return True if the request's valid for given criteria inside the context.
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	boolean validate(Map<String, String> request) throws Exception;
}
