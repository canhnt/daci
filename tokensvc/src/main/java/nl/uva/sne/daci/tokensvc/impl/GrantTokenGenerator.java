package nl.uva.sne.daci.tokensvc.impl;

import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.uva.sne.daci._1_0.schema.GrantTokenType;
import nl.uva.sne.daci._1_0.schema.ObjectFactory;
import nl.uva.sne.daci._1_0.schema.RequestType;
import nl.uva.sne.daci.tokensvc.utils.XMLUtil;

import org.w3._2000._09.xmldsig.KeyInfoType;
import org.w3c.dom.Document;

public class GrantTokenGenerator {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GrantTokenGenerator.class);
	
	
	private Certificate signingCert;


	private PrivateKey signingKey;
	
	
	public GrantTokenGenerator(PrivateKey signingKey, Certificate signingCert){
		this.signingKey = signingKey;
		this.signingCert = signingCert; 
	}
			
	/**
	 * Sign the DOM object.
	 * 
	 * @param doc The DOM object to be signed.
	 * @return The signed DOM object.
	 * @throws Exception
	 */
	private Document createEnvelopedSignature(Document doc) throws Exception {
		
        // First, create a DOM XMLSignatureFactory that will be used to 
        // generate the XMLSignature and marshal it to DOM.
//        String providerName = System.getProperty("jsr105Provider", "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI");
//        
//        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());
			
//		String providerName = System.getProperty("jsr105Provider", "org.apache.jcp.xml.dsig.internal.XMLDSigRI");

//		String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
		
//        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", new org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI());
//        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        
        if (fac == null)
        	throw new Exception("Cannot load XMLSignature factory");

        // Create a Reference to the enveloped document (in this case we are
        // signing the whole document, so a URI of "" signifies that) and
        // also specify the SHA1 digest algorithm and the ENVELOPED Transform.
        Transform envelopedTransform = fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        Transform c14N11Transform = fac.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec)null);
        
        List<Transform> transformList = new ArrayList<Transform>();        
        transformList.add(envelopedTransform);        
        transformList.add(c14N11Transform);
        		        		
        Reference ref = fac.newReference("", 
        	fac.newDigestMethod(DigestMethod.SHA1, null), transformList, null, null);

        // Create the SignedInfo
        SignedInfo si = fac.newSignedInfo(
        		fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null), 
        		fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
        		Collections.singletonList(ref));
		
        // Instantiate the document to be signed
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        dbf.setNamespaceAware(true);
//        Document doc = dbf.newDocumentBuilder().newDocument();
//        Document doc = docToken;
                
		KeyInfoFactory kif = fac.getKeyInfoFactory();
		KeyValue kv = kif.newKeyValue(this.signingCert.getPublicKey());
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv)); 
        
        // Create a DOMSignContext and specify the PrivateKey and
        // location of the resulting XMLSignature's parent element
        DOMSignContext dsc = new DOMSignContext(this.signingKey, doc.getDocumentElement());

        // Create the XMLSignature (but don't sign it yet)
        XMLSignature signature = fac.newXMLSignature(si, ki);

        // Marshal, generate (and sign) the enveloped signature
        signature.sign(dsc);
        
        return doc;

	}

	/**
	 * Create a grant token from elements.
	 * 
	 * @param tenantId
	 * @param request
	 * @param userKeyInfo
	 * @return
	 */
	private GrantTokenType generateGrantToken(String tenantId, RequestType request, KeyInfoType userKeyInfo) {
		
		ObjectFactory fac = new ObjectFactory();
		GrantTokenType gToken = fac.createGrantTokenType();
		
		gToken.setTenantId(tenantId);
		gToken.setRequest(request);
		gToken.setKeyInfo(userKeyInfo);
		
		return gToken;
	}

	/**
	 * Create a grant token from elements, then sign using provider's private key.
	 *  
	 * @param tenantId
	 * @param request
	 * @param userKeyInfo
	 * @return
	 * @throws Exception
	 */
	public Document generateAndSign(String tenantId, RequestType request, KeyInfoType userKeyInfo) throws Exception {
		
		//construct the grant token
		log.debug("Creating a token for tenant: {}", tenantId);
		GrantTokenType gToken = generateGrantToken(tenantId, request, userKeyInfo);
				
		Document doc = marshal(gToken);
		
		Document signedDoc = createEnvelopedSignature(doc);
		
		return signedDoc;
	}
	

//	public Document generateAndSign(String tenantId, String request, String userKeyInfo) throws Exception {
//		if (tenantId == null || tenantId.isEmpty())
//			throw new IllegalArgumentException("tenantId parameter must not be null");
//
//		if (request == null || request.isEmpty())
//			throw new IllegalArgumentException("request parameter must not be null");
//		
//		if (userKeyInfo == null || userKeyInfo.isEmpty())
//			throw new IllegalArgumentException("userKeyInfo parameter must not be null");		
//
//		RequestType requestObject;
//		try {
//			requestObject = convertRequest(request);
//		} catch (ParserConfigurationException | SAXException | IOException e) {
//			log.error("Error converting request:" + e.getMessage());
//			throw e;
//		}
//		
//		KeyInfoType keyInfoObj;
//		try {
//			keyInfoObj = convertKeyInfo(userKeyInfo);
//		} catch (ParserConfigurationException | SAXException | IOException e) {
//			log.error("Error converting keyinfo:" + e.getMessage());
//			throw e;
//		}		
//		
//		Document doc = generateAndSign(tenantId, requestObject, keyInfoObj);		
//		return doc;
//	}

//	private KeyInfoType convertKeyInfo(String keyinfo) throws ParserConfigurationException, SAXException, IOException {
//		InputStream istream = new ByteArrayInputStream(keyinfo.getBytes("UTF-8"));
//						
//		return XMLUtil.unmarshal(KeyInfoType.class, istream);
//	}


//	private RequestType convertRequest(String request) throws ParserConfigurationException, SAXException, IOException {
//		InputStream istream = new ByteArrayInputStream(request.getBytes("UTF-8"));
//		
//		return XMLUtil.unmarshal(RequestType.class, istream);
//	}
	
//	public static Document marshal(GrantTokenType token) throws Exception {
//		// Marshal token to DOM object		
//		ObjectFactory fac = new ObjectFactory();
//		JAXBElement<GrantTokenType> jaxb = fac.createGrantToken(token);
//		
//		DOMResult res = new DOMResult();
//		JAXBContext jc = JAXBContext.newInstance(jaxb.getClass());
//		Marshaller m = jc.createMarshaller();
//		m.marshal(jaxb, res);
//	
//		Document doc = (Document) res.getNode();
//			
//		return doc;
//	}

	public static Document marshal(GrantTokenType token) throws ParserConfigurationException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		
		ObjectFactory fac = new ObjectFactory();
		JAXBElement<GrantTokenType> jaxb = fac.createGrantToken(token);
		
		XMLUtil.print(jaxb, GrantTokenType.class, doc);
		return doc;		
	}
}
