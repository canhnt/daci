package nl.uva.sne.daci.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import nl.uva.sne.xacml.policy.parsers.util.DataTypeConverterUtil;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributesType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;

public class XACMLUtil extends nl.uva.sne.xacml.util.XACMLUtil {

	private static final String XACML_ATTR_CATEGORY_SUBJECT = "urn:oasis:names:tc:xacml:3.0:attribute-category:subject";

	private static final String XACML_ATTR_CATEGORY_RESOURCE = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";

	private static final String XACML_ATTR_CATEGORY_ACTION = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";

	private static final String XACML_ATTR_CATEGORY_ENVIRONMENT = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";

	private static final String XACML_ATTR_SUBJECT_PREFIX = "urn:oasis:names:tc:xacml:1.0:subject";
	
	private static final String XACML_ATTR_RESOURCE_PREFIX = "urn:oasis:names:tc:xacml:1.0:resource";
	
	private static final String XACML_ATTR_ACTION_PREFIX = "urn:oasis:names:tc:xacml:1.0:action";

	private static final String XACML_ATTR_ENV_PREFIX = "urn:oasis:names:tc:xacml:1.0:environment";;
	
	private enum AttributeCategory {
		SUBJECT_CATEGORY,
		RESOURCE_CATEGORY,
		ACTION_CATEGORY,
		ENVIRONMENT_CATEGORY
	}
	
	private ObjectFactory fac;

	public XACMLUtil() {
		fac = new ObjectFactory();
	}
	public RequestType createRequest(Map<String, String> attributes) {
		
		RequestType request = fac.createRequestType();
		
		List<AttributeType> subAttrs = new ArrayList<AttributeType>();
		List<AttributeType> resAttrs = new ArrayList<AttributeType>();
		List<AttributeType> actAttrs = new ArrayList<AttributeType>();
		List<AttributeType> envAttrs = new ArrayList<AttributeType>();
		
		for(String attrId : attributes.keySet()) {
			AttributeType attr = createAttribute(attrId, attributes.get(attrId));
			switch(getAttributeCategory(attrId)) {
			case SUBJECT_CATEGORY:
				subAttrs.add(attr); 
				break;
			case RESOURCE_CATEGORY:
				resAttrs.add(attr); 
				break;
			case ACTION_CATEGORY:
				subAttrs.add(attr); 
				break;
			case ENVIRONMENT_CATEGORY:
				actAttrs.add(attr); 
				break;				
			}			
		}
				
		request.getAttributes().add(createAttributes(subAttrs, AttributeCategory.SUBJECT_CATEGORY));				
		request.getAttributes().add(createAttributes(resAttrs, AttributeCategory.RESOURCE_CATEGORY));
		request.getAttributes().add(createAttributes(actAttrs, AttributeCategory.ACTION_CATEGORY));
		request.getAttributes().add(createAttributes(envAttrs, AttributeCategory.ENVIRONMENT_CATEGORY));
		
		return request;
	}

	private AttributesType createAttributes(List<AttributeType> attributes, AttributeCategory cat) {
		AttributesType attrs = fac.createAttributesType();
		
		String attrCat = null;
		switch (cat){
		case SUBJECT_CATEGORY:
			attrCat = XACML_ATTR_CATEGORY_SUBJECT; break;
		case RESOURCE_CATEGORY:
			attrCat = XACML_ATTR_CATEGORY_RESOURCE; break;
		case ACTION_CATEGORY:
			attrCat = XACML_ATTR_CATEGORY_ACTION; break;
		case ENVIRONMENT_CATEGORY:
			attrCat = XACML_ATTR_CATEGORY_ENVIRONMENT; break;			
		}
		
		attrs.setCategory(attrCat);
		attrs.getAttribute().addAll(attributes);
		
		return attrs;
	}
	
	private static AttributeCategory getAttributeCategory(String attrId) {

		if (attrId.startsWith(XACML_ATTR_SUBJECT_PREFIX))
			return AttributeCategory.SUBJECT_CATEGORY;
		else if (attrId.startsWith(XACML_ATTR_RESOURCE_PREFIX))
			return AttributeCategory.RESOURCE_CATEGORY;
		else if (attrId.startsWith(XACML_ATTR_ACTION_PREFIX))
			return AttributeCategory.ACTION_CATEGORY;
		else if (attrId.startsWith(XACML_ATTR_ENV_PREFIX))
			return AttributeCategory.ENVIRONMENT_CATEGORY;
				
		throw new RuntimeException("Unknown attribute category:" + attrId);
	}
	private AttributeType createAttribute(String id, String value) {
		AttributeType attr = fac.createAttributeType();
		attr.setAttributeId(id);
		
		AttributeValueType attrValue = fac.createAttributeValueType();
		attrValue.setDataType(DataTypeConverterUtil.XACML_3_0_DATA_TYPE_STRING);
		attrValue.getContent().add(value);
		
		attr.getAttributeValue().add(attrValue);
		
		return attr;
	}	
	
	public static String marshal(PolicyType p) {
		OutputStream os = new ByteArrayOutputStream();
		
		JAXBElement<PolicyType> jaxb = (new ObjectFactory()).createPolicy(p);
		nl.uva.sne.xacml.util.XACMLUtil.print(jaxb, PolicyType.class, os);
		
		return os.toString();
	}
	
	public static String marshal(PolicySetType ps) {
		OutputStream os = new ByteArrayOutputStream();
		
		
		JAXBElement<PolicySetType> jaxb = (new ObjectFactory()).createPolicySet(ps);
		nl.uva.sne.xacml.util.XACMLUtil.print(jaxb, PolicySetType.class, os);
		
		return os.toString();
		
	}
}
