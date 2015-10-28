package nl.uva.sne.daci.context.impl;

import java.util.HashMap;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributesType;
import nl.uva.sne.daci.context.Context;
import nl.uva.sne.midd.Decision;
import nl.uva.sne.midd.DecisionType;
import nl.uva.sne.midd.Variable;
import nl.uva.sne.midd.nodes.InternalNode;
import nl.uva.sne.midd.utils.EvaluationUtil;
import nl.uva.sne.xacml.AttributeMapper;
import nl.uva.sne.xacml.policy.parsers.MIDDParsingException;
import nl.uva.sne.xacml.policy.parsers.XACMLParsingException;
import nl.uva.sne.xacml.policy.parsers.util.AttributeConverter;
import nl.uva.sne.xacml.policy.parsers.util.DataTypeConverterUtil;

public class ContextImpl implements Context {

	private InternalNode<?> criteria;
	
	private Map<String, String> issuerAttributes;
	
	private AttributeMapper attrMapper;
	
	public ContextImpl(Map<String, String> issuerAttributes, InternalNode<?> ctxCriteria, AttributeMapper attrMapper) {
		this.issuerAttributes = issuerAttributes;
		this.criteria = ctxCriteria;
		this.attrMapper = attrMapper;
	}
	
	@Override
	public Map<String, String> getIssuerAttributes() {
		return this.issuerAttributes;
	}

	@Override
	public boolean validate(Map<String, String> request) throws Exception {
		
		Map<Integer, Variable<?>> variables = convertRequest(request);
		
		Decision result = EvaluationUtil.eval(this.criteria, variables);
		
		return result.getDecision() == DecisionType.Permit;
			
	}

	private Map<Integer, Variable<?>> convertRequest(Map<String, String> request) throws MIDDParsingException, XACMLParsingException {
		Map<Integer, Variable<?>> variables = new HashMap<Integer, Variable<?>>();
		
		AttributeConverter attrConverter = new AttributeConverter(this.attrMapper);
		
		
		for(String attrId : request.keySet()) {
			
			int varId;
			if (!attrMapper.hasVariableId(attrId)) {
				varId = attrMapper.addAttribute(attrId);
			} else
				varId = attrMapper.getVariableId(attrId);
			
			Comparable value = DataTypeConverterUtil.convert(request.get(attrId), DataTypeConverterUtil.XACML_3_0_DATA_TYPE_STRING);
			Variable<?> var = new Variable(varId, value);
			
			variables.put(var.getID(), var);	
		}
		
		return variables;
	}
}
