package nl.uva.sne.daci.context;

import java.util.HashMap;
import java.util.Map;

import nl.uva.sne.daci.context.impl.ContextRequestImpl;

public class ContextRequestBuilder {
	private static final String SUBJECT_ATTRIBUTE_PREFIX = "urn:oasis:names:tc:xacml:1.0:subject";
	
	private static final String RESOURCE_ATTRIBUTE_PREFIX = "urn:oasis:names:tc:xacml:1.0:resource";
	
	private static final String ACTION_ATTRIBUTE_PREFIX = "urn:oasis:names:tc:xacml:1.0:action";
	
	private static final String ENV_ATTRIBUTE_PREFIX = "urn:oasis:names:tc:xacml:1.0:environment";

	private static final String SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
	/**
	 * Create a context request <subject, permission> from a vector of attributes.
	 *  
	 * @param attributes
	 * @return
	 */
	public static ContextRequest create(Map<String, String> attributes){
		
		Map<String, String> subjectAttributes = new HashMap<String, String>();
		
		Map<String, String> permissionAttributes = new HashMap<String, String>();
		
		for(String attrId : attributes.keySet()) {
			if (attrId.startsWith(SUBJECT_ATTRIBUTE_PREFIX)) {
				subjectAttributes.put(attrId, attributes.get(attrId));
			}
			else if (attrId.startsWith(RESOURCE_ATTRIBUTE_PREFIX) || 
					 attrId.startsWith(ACTION_ATTRIBUTE_PREFIX) || 
					 attrId.startsWith(ENV_ATTRIBUTE_PREFIX)) {
				permissionAttributes.put(attrId, attributes.get(attrId));
			}
		}
		
		ContextRequestImpl ctxRequest = new ContextRequestImpl(subjectAttributes, permissionAttributes);
		
		return ctxRequest;
	}
	
	public static ContextRequest create(String tenantId, Map<String, String> attributes){
		Map<String, String> subjectAttributes = new HashMap<String, String>();
		subjectAttributes.put(SUBJECT_ID, tenantId);
		
		Map<String, String> permissionAttributes = new HashMap<String, String>();
		
		for(String attrId : attributes.keySet()) {
			if (attrId.startsWith(RESOURCE_ATTRIBUTE_PREFIX) || 
				attrId.startsWith(ACTION_ATTRIBUTE_PREFIX) || 
				attrId.startsWith(ENV_ATTRIBUTE_PREFIX)) {
				permissionAttributes.put(attrId, attributes.get(attrId));
			}
		}
		
		ContextRequestImpl ctxRequest = new ContextRequestImpl(subjectAttributes, permissionAttributes);
		
		return ctxRequest;		
	}

}
