package nl.uva.sne.daci.context;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;

import nl.uva.sne.daci.utils.XACMLUtil;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;

import org.xml.sax.SAXException;

public class PolicySetupUtil {
	public static String loadPolicySet(String policysetFile) throws Exception{
		
		PolicySetType psRoot = XACMLUtil.unmarshalPolicySetType(policysetFile);
		if (psRoot != null && psRoot.getPolicySetId() != null) {
			List<JAXBElement<?>> jaxbs = psRoot.getPolicySetOrPolicyOrPolicySetIdReference();
			if (jaxbs != null && jaxbs.size() > 0)
				return XACMLUtil.marshal(psRoot);
		}
		
		fail("Unable to load policyset");
		return null;
	}

	public static Map<String, String> loadPolicies(String xmlFile) throws ParserConfigurationException, SAXException, IOException {
		
		PolicySetType psRoot = XACMLUtil.unmarshalPolicySetType(xmlFile);
		
		// map of <policyId, policy_data>
		Map<String, String> policies = new HashMap<String, String>();
		for(JAXBElement<?> jaxbElement : psRoot.getPolicySetOrPolicyOrPolicySetIdReference()) {
			
			Object value = jaxbElement.getValue();
			
			if (value instanceof PolicySetType) {
				
				PolicySetType ps = (PolicySetType)value;				
				policies.put(ps.getPolicySetId(), XACMLUtil.marshal(ps));
			} else if (value instanceof PolicyType) {
				
				PolicyType p = (PolicyType)value;				
				policies.put(p.getPolicyId(), XACMLUtil.marshal(p));
			} else {
				System.err.println("Unknown object data type:" + value.toString());
			}
		}
		
		return policies;
	}
}
