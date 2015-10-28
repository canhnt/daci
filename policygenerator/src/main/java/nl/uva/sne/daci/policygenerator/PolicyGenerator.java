package nl.uva.sne.daci.policygenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MatchType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.TargetType;
import nl.uva.sne.daci.policygenerator.vi.VirtualInfrastructure;
import nl.uva.sne.daci.policygenerator.vi.Resource;
import nl.uva.sne.xacml.policy.parsers.util.CombiningAlgConverterUtil;
import nl.uva.sne.xacml.policy.parsers.util.DataTypeConverterUtil;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class PolicyGenerator {
	
	private static final String POLICY_PREFIX = "";


	private static final String STRING_EQUAL_OPERATOR = "urn:oasis:names:tc:xacml:1.0:function:string-equal";

	private static final String XACML_ATTR_CATEGORY_RESOURCE = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";

	private static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";


	private static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";

	private static final String SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
	
	private static final String SUBJECT_ROLE = "urn:oasis:names:tc:xacml:1.0:subject:subject-role";
	
//	private static final String TENANT_ID = "urn:eu:geysers:daci:tenants:tenant-id";


	private OntModel model;

	ObjectFactory fac;
	
	AttributeTemplate at;
	
	Map<String, VirtualInfrastructure> VIs;
	
	public PolicyGenerator(OntModel model, AttributeTemplate at) {
		this.model = model;
		this.at = at;
		
		VIs = new HashMap<String, VirtualInfrastructure>();
		fac = new ObjectFactory();
		
		getVI();
	}
	
	/**
	 * Generate delegation policies from the provider (original owner) to tenants, based on their VI descriptions.
	 * @return
	 */
	public PolicySetType generateProviderPolicies() {
				
		List<PolicyType> policies = new ArrayList<PolicyType>(VIs.size());
		
		for(String viGRI: VIs.keySet()) {
			PolicyType p = generatePolicy(VIs.get(viGRI));
			policies.add(p);
		}
		
		String psId = "PS-0";
		return createPolicySet(psId, policies, CombiningAlgConverterUtil.XACML_3_0_RULE_COMBINING_ALGO_DENY_UNLESS_PERMIT);
		
	}
	
	private PolicySetType createPolicySet(String psId, List<PolicyType> policies, String pca) {
		PolicySetType ps = fac.createPolicySetType();
		
		ps.setPolicySetId(psId);
		ps.setPolicyCombiningAlgId(pca);
		List<JAXBElement<?>> jaxbList = ps.getPolicySetOrPolicyOrPolicySetIdReference();
		
		for(PolicyType c : policies) {
			jaxbList.add(fac.createPolicy(c));
		}

		return ps;
	}

	private void getVI() {
		String queryString =        
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"PREFIX imf: <http://geysers.eu/imf.owl#>\n" +
				"SELECT ?vi ?r ?rtype \n" +
				"WHERE {\n" +
				"	?vi rdf:type imf:VirtualInfrastructure.\n" +
				"	?vi imf:hasResource ?r.\n" +
				"	?r rdf:type ?rtype.\n" +
				"	?rtype rdfs:subClassOf* imf:Resource.\n" +
				"}";
		
		Query query = QueryFactory.create(queryString);
		query.addResultVar("?vi");
		query.addResultVar("?r");
		query.addResultVar("?type");
		
	    QueryExecution qe = QueryExecutionFactory.create(query, model);
	    ResultSet results =  qe.execSelect();
		
		while (results.hasNext()){
			QuerySolution qs = results.nextSolution();
			String viId = qs.get("?vi").toString();
			
			VirtualInfrastructure vi = VIs.get(viId);
			if (vi == null) {
				 vi = new VirtualInfrastructure(viId);
				 VIs.put(viId, vi);
			} 
			
			String rId = qs.get("?r").toString();
			String rType = parseType(qs.get("?rtype").toString());
			vi.addResource(new Resource(rId, rType));
			
		}
	    qe.close();
	}
	
	private String parseType(String imfType) {
		String[] s = imfType.split("#");
		if (s.length == 2 && s[0].equalsIgnoreCase("http://geysers.eu/imf.owl"))
			return "imf:"+s[1];
		else
			throw new RuntimeException("Unknown IMF resource type");
	}

	private PolicyType generatePolicy(VirtualInfrastructure vi) {
		PolicyType p = fac.createPolicyType();
		
		String policyId = POLICY_PREFIX + vi.getId();
		p.setPolicyId(policyId);
		p.setRuleCombiningAlgId(CombiningAlgConverterUtil.XACML_3_0_POLICY_COMBINING_ALGO_PERMIT_OVERRIDES);
		
		TargetType t = fac.createTargetType();
		AnyOfType anyOf = fac.createAnyOfType();		
		AllOfType allOf = fac.createAllOfType();
		
		MatchType m = createMatchType(STRING_EQUAL_OPERATOR, SUBJECT_ID, vi.getId());
				
		allOf.getMatch().add(m);		
		anyOf.getAllOf().add(allOf);
		t.getAnyOf().add(anyOf);
		p.setTarget(t);
		
		for(Resource res:vi.getResources()) {
			RuleType rule = createRule(res);
			p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().add(rule);
		}
		
		return p;
	}


	private RuleType createRule(Resource res) {
		RuleType rule = fac.createRuleType();
		
		rule.setRuleId(res.getId());
		rule.setEffect(EffectType.PERMIT);
		
		TargetType t = fac.createTargetType();
		AnyOfType resIdAnyOf = fac.createAnyOfType();
		
		AllOfType allOfResId = fac.createAllOfType();
		allOfResId.getMatch().add(createMatchType(STRING_EQUAL_OPERATOR, RESOURCE_ID, res.getId()));		
		resIdAnyOf.getAllOf().add(allOfResId);
		
		AnyOfType actionsAnyOf = fac.createAnyOfType(); 
		List<Attribute> actionAttrs = at.loadActionAttributes(res.getType());
		for(Attribute attr:actionAttrs) {
			AllOfType allOf = fac.createAllOfType();
			allOf.getMatch().add(createMatchType(STRING_EQUAL_OPERATOR, ACTION_ID, attr.getValue()));
			
			actionsAnyOf.getAllOf().add(allOf);
		}
		
		t.getAnyOf().add(resIdAnyOf);
		t.getAnyOf().add(actionsAnyOf);
		rule.setTarget(t);
		return rule;
	}


	private MatchType createMatchType(String matchId, String attrId, String attrValue) {
		
		ObjectFactory fac = new ObjectFactory();
		MatchType m = fac.createMatchType();
								
		AttributeDesignatorType ad = fac.createAttributeDesignatorType();
		ad.setAttributeId(attrId);
		ad.setDataType(DataTypeConverterUtil.XACML_3_0_DATA_TYPE_STRING);
		ad.setMustBePresent(true);
		ad.setCategory(XACML_ATTR_CATEGORY_RESOURCE);		
		
		AttributeValueType av = fac.createAttributeValueType();
		av.setDataType(DataTypeConverterUtil.XACML_3_0_DATA_TYPE_STRING);
		av.getContent().add(attrValue);
		
		m.setMatchId(matchId);
		m.setAttributeDesignator(ad);
		m.setAttributeValue(av);
	
		return m;
	}

	/**
	 * Each policy in the policyset represents a sharing rule between a tenant (trustor) with another (trustee):
	 *  - Tenant i shares 1-2 resources with tenant[i+1], i = 0..(n-1) (i.e. tenant[n-1] shares with tenant[0])
	 * @return
	 */
	public PolicySetType generateInterTenantPolicies() {
		
		String[] tenantIds = VIs.keySet().toArray(new String[0]);

		List<PolicyType> policies = new ArrayList<PolicyType>(VIs.size());
		for(int i = 0; i < tenantIds.length; i++) {
			String trustorId = VIs.get(tenantIds[i]).getId();	
			String trusteeId = VIs.get(tenantIds[(i+1) % tenantIds.length]).getId();
			
			PolicyType p = generateInterTenantPolicy(VIs.get(trustorId), trusteeId);
			policies.add(p);
			
		}
	
		return createPolicySet("InterTenantPolicies", policies, CombiningAlgConverterUtil.XACML_3_0_RULE_COMBINING_ALGO_DENY_UNLESS_PERMIT);		 
	}

	/**
	 * Create a policy sharing the first resource of indicated VI to the trustee.
	 * 
	 * @param vi
	 * @param trusteeId
	 * @return
	 */
	private PolicyType generateInterTenantPolicy(VirtualInfrastructure vi, String trusteeId) {
			
		PolicyType p = fac.createPolicyType();
		
		String policyId = POLICY_PREFIX + vi.getId();
		p.setPolicyId(policyId);
		p.setRuleCombiningAlgId(CombiningAlgConverterUtil.XACML_3_0_POLICY_COMBINING_ALGO_PERMIT_OVERRIDES);
		
		TargetType t = fac.createTargetType();
		AnyOfType anyOf = fac.createAnyOfType();		
		AllOfType allOf = fac.createAllOfType();
		
		MatchType m = createMatchType(STRING_EQUAL_OPERATOR, SUBJECT_ID, trusteeId);
				
		allOf.getMatch().add(m);		
		anyOf.getAllOf().add(allOf);
		t.getAnyOf().add(anyOf);
		p.setTarget(t);
		
		Resource res = vi.getResources().get(0);
		RuleType rule = createRule(res);
		p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().add(rule);
		
		return p;
	}
	
	/**
	 * Create intra-tenant policies for tenants: 
	 * 	- Each tenant has own policies for its end-users.
	 *  - A rule for shared resource from the trustor, VIs[i-1]
	 *   
	 * @return
	 */
	public PolicySetType generateIntraTenantPolicies() {
		String[] tenantIds = VIs.keySet().toArray(new String[0]);

		List<PolicyType> policies = new ArrayList<PolicyType>(VIs.size());

		for(int i = 0; i < tenantIds.length; i++) {
			String trustorId = VIs.get(tenantIds[i]).getId();	
			String trusteeId = VIs.get(tenantIds[(i+1) % tenantIds.length]).getId();
			
			PolicyType p = generateIntraTenantPolicy(trustorId, VIs.get(trusteeId));
			policies.add(p);
			
		}
		
//		for(String tenantId : VIs.keySet()) {
//			
//			PolicyType p = generateIntraTenantPolicy(VIs.get(tenantId), tenantId);
//			policies.add(p);			
//		}
	
		return createPolicySet("IntraTenantPolicies", policies, CombiningAlgConverterUtil.XACML_3_0_RULE_COMBINING_ALGO_DENY_UNLESS_PERMIT);		 
	}

	/**
	 * Create a policy for a given VI, with subject-role is the 'admin' role. 
	 * This is the intra-tenant policy for a given tenant, that allows users belong to 'admin' role can access VI's resoures. 
	 * 
	 * @param vi
	 * @param tenantId
	 * @return
	 */
	private PolicyType generateIntraTenantPolicy(
			String trustorId, VirtualInfrastructure vi) {

		PolicyType p = fac.createPolicyType();
		
		String policyId = POLICY_PREFIX + vi.getId();
		
		p.setPolicyId(policyId);
		p.setRuleCombiningAlgId(CombiningAlgConverterUtil.XACML_3_0_POLICY_COMBINING_ALGO_PERMIT_OVERRIDES);
		
		TargetType t = fac.createTargetType();
		AnyOfType anyOf = fac.createAnyOfType();		
		AllOfType allOf = fac.createAllOfType();
		
		MatchType m = createMatchType(STRING_EQUAL_OPERATOR, SUBJECT_ROLE, "admin");
				
		allOf.getMatch().add(m);		
		anyOf.getAllOf().add(allOf);
		t.getAnyOf().add(anyOf);
		p.setTarget(t);
		
		for(Resource res:vi.getResources()) {
			RuleType rule = createRule(res);
			p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().add(rule);
		}
		
		// create the rule for shared resource from trustor
		VirtualInfrastructure trustorVI = VIs.get(trustorId);
		Resource sharedRes = trustorVI.getResources().get(0);
		RuleType shareRule = createRule(sharedRes);
		p.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().add(shareRule);
		
		return p;
	}	
}
