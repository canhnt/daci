/**
 * 
 */
package nl.uva.sne.daci.context;

import java.util.Map;

/**
 * @author canhnt
 * The ContextRequest split subject with resource and env attributes.
 */
public interface ContextRequest {
	/**
	 * Return the subject attributes of the request.
	 * @return
	 */
	Map<String, String> getSubjectAttributes();
	
	/**
	 * Return vector of permission attributes, including resource and environment attributes.
	 * @return
	 */
	Map<String, String> getPermissionAttributes();
	
	/**
	 * Return all attributes
	 * @return
	 */
	Map<String, String> getAttributes();
	
	
}
