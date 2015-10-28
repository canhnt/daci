package nl.uva.sne.daci.tokensvc;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import nl.uva.sne.daci._1_0.schema.AttributeType;
import nl.uva.sne.daci._1_0.schema.AttributeValueType;
import nl.uva.sne.daci._1_0.schema.AttributesType;
import nl.uva.sne.daci._1_0.schema.ObjectFactory;
import nl.uva.sne.daci._1_0.schema.RequestType;
import nl.uva.sne.daci.tokensvc.impl.TokenSvcImpl;
import nl.uva.sne.daci.tokensvc.utils.XMLUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig.KeyInfoType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TokenSvcImplTest {
	TokenSvcImpl tokensvc;

	private static final String DUMMY_KEYINFO = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">" +
			"<KeyValue><RSAKeyValue><Modulus>t6JBE0eWhmqCbrUWpm9qF3NR933zxrj2oMdRSAX/i5GemTKpuJAFION1n4RKA13F8hs0ITrJZ//M" +
			"lnuidivrR4zNJldUmZw2m7piDxhi6/YOB6uvZmKuMwFVDfx8TAGVzb3sgMCp/31WiWfFsTWPeSqn" +
			"VzTZhZT6RUJYj12UVHpFFHkvpZvTXvBp9PQaFL25dws36P027ZdoUYr28MUsO19+OHddfXD6VnkC" +
			"mKsY8n5lb+LQZ6vFQLVlebvTsclspt97y5fEk10CJXMNk7N+I/PVEVrTkjoMR+eu8iOeSxF7JPzV" +
			"vS+8iYkeO0aJG0G3CqEtm27j/5U8rrbFjjd4Uw==</Modulus><Exponent>AQAB</Exponent></RSAKeyValue></KeyValue>" +
			"</ds:KeyInfo>";
	
	@Before
	public void setUp() throws Exception {
		tokensvc = new TokenSvcImpl();
		
		tokensvc.setBaseDir("/home/canhnt/src/workspace/daci/tokensvc/etc/tokensvc/");
		tokensvc.setKeyAlias("tokensvc");
		tokensvc.setKeyPassword("tokensvc-cloud");
		tokensvc.setKeyStore("tokensvc-keystore.jks");
		tokensvc.setKeyStorePassword("cloudsecurity");
		tokensvc.setTrustedKeyStore("trusted-keystore.jks");
		tokensvc.setTrustedKeyStorePassword("trusted");
		
		tokensvc.init();
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testIssueGrantToken() {
		
		
		long startTime = System.currentTimeMillis();			
		
		for(int i = 0; i < 100; i++) {
			RequestType r = generateRequest(0);
			KeyInfoType ki =generateKeyInfo();			
			String token = tokensvc.issueGrantToken("http://demo3.sne.uva.nl/VI/750", r, ki);
		}
					
		long stopTime = System.currentTimeMillis();		
		long nExecTime = stopTime - startTime;
		

		System.out.println("Issuing speed: " + nExecTime/1000 + "ms/req");
	}

	public static RequestType generateRequest(int numAttrExtra) {
		
		ObjectFactory fac = new ObjectFactory();
		RequestType request = fac.createRequestType();
				
		AttributeType subjIdAttr = createAttributeType(fac, "subject-id", 
				"string", 
				"http://demo3.sne.uva.nl/VI/750/");

		AttributesType subj = fac.createAttributesType();
		subj.getAttribute().add(subjIdAttr);
		request.setSubjectAttributes(subj);

		// create resource attr
		AttributeType res = createAttributeType(fac, "resource-id", 
				"string", 
				"http://demo3.uva.nl/vi/745/ComputingNode");
		
		// create action attr
		AttributeType action = createAttributeType(fac, "action-id", 
				"string", 
				"SLI:Operate-VR:Stop");
				
		AttributesType permission = fac.createAttributesType();
		permission.getAttribute().add(res);
		permission.getAttribute().add(action);
				
		// add some extra attributes 
		for(int i = 0; i < numAttrExtra; i++) {
			AttributeType extraAttr = createAttributeType(fac, "attrid" + i, 
					"http://www.w3.org/2001/XMLSchema#string", 
					"AttrVal" + i);
			permission.getAttribute().add(extraAttr);
			
		}
		
		request.setPermissionAttributes(permission);

		return request;	}

	private static AttributeType createAttributeType(ObjectFactory fac, String attrId,
			String dataType, String value) {
		
		AttributeValueType attrVal = fac.createAttributeValueType();		
		attrVal.setDataType(dataType);
		attrVal.setValue(value);
		
		AttributeType attr = fac.createAttributeType();
		attr.setAttributeId(attrId);		
		attr.setAttributeValue(attrVal);
		return attr;
	}

	public static KeyInfoType generateKeyInfo() {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(DUMMY_KEYINFO.getBytes());
			Document docKeyInfo =  XMLUtil.readXML(bis);
			
	        KeyInfoType keyInfo = XMLUtil.unmarshal(KeyInfoType.class, docKeyInfo.getDocumentElement());
	        return keyInfo;
		} catch (ParserConfigurationException | SAXException | IOException e) {

			e.printStackTrace();
		}
		return null;
	}

}
