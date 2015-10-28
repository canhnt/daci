package nl.uva.sne.daci.tokensvc;

import java.io.IOException;

import javax.xml.bind.JAXBElement;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import nl.uva.sne.daci._1_0.schema.GrantTokenType;
import nl.uva.sne.daci._1_0.schema.ObjectFactory;
import nl.uva.sne.daci.tokensvc.utils.XMLUtil;

public class XMLSignatureDemo {
	public static final String TOKEN_FILE = "src/test/resources/grant-token.xml";
	
	public static final String KEYSTORE_FILE = "src/test/resources/tokensvc-kestore.jks";

	private static final String KEYSTORE_PASSWORD = "cloudsecurity";

	private static final String KEY_ALIAS = "tokensvc";

	private static final String KEYPASS = "tokensvc-cloud";

	private static final String SIGNED_TOKEN_FILE = "src/test/resources/grant-token-signature.xml";
	
	private Certificate signingCert;
	public static void main(String[] args) throws Exception {
		
		installProvider();
		
		Document docToken = XMLUtil.readXML(TOKEN_FILE);
		
//		Document docToken = XMLUtil.readXML(SIGNED_TOKEN_FILE);
//				
//		printDOM(docToken);
		
//		GrantTokenType token = XMLUtil.unmarshalGrantToken(new FileInputStream(SIGNED_TOKEN_FILE));
		
//		GrantTokenType token = unmarshal(docToken);
//		if (token != null && token.getSignature() != null) {
//			System.out.println("Got signature in token");
//		} else
//			System.err.println("No signature in token");
//		
//		Document doc2 = marshal(token);
//		System.out.println("Print again:");
//		printDOM(doc2);
		
		XMLSignatureDemo demo = new XMLSignatureDemo();
		demo.saveSignature(SIGNED_TOKEN_FILE, demo.createEnvelopedSignature(docToken));
								
		System.out.println("Verifying signature at: " + SIGNED_TOKEN_FILE);
		if (demo.validateSignature(SIGNED_TOKEN_FILE))
			System.out.println("Done");
		else 
			System.out.println("Failed");
	}

	private static void printDOM(Document doc) throws TransformerException {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(doc),
		      new StreamResult(buffer));
		String str = buffer.toString();
		System.out.println(str);
		
	}

	private static Document marshal(GrantTokenType token) throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		
		ObjectFactory fac = new ObjectFactory();
		JAXBElement<GrantTokenType> jaxb = fac.createGrantToken(token);
		
		XMLUtil.print(jaxb, GrantTokenType.class, doc);
		return doc;
	}

	private static GrantTokenType unmarshal(Document doc) {

		GrantTokenType t = XMLUtil.unmarshal(GrantTokenType.class, doc.getDocumentElement());
		
		return t;
	}

	private boolean validateSignature(String signedDocFile) throws Exception {
	       // Instantiate the document to be validated
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new FileInputStream(signedDocFile));
				
        // Find Signature element
        NodeList nl = 
            doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        }

        // Create a DOM XMLSignatureFactory that will be used to unmarshal the 
        // document containing the XMLSignature 
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // Create a DOMValidateContext and specify a KeyValue KeySelector
        // and document context
        DOMValidateContext valContext = new DOMValidateContext(new KeyValueKeySelector(), nl.item(0));
                
        // unmarshal the XMLSignature
        XMLSignature signature = fac.unmarshalXMLSignature(valContext);        
        
        
        // Validate the XMLSignature (generated above)
        boolean coreValidity = signature.validate(valContext); 

        // Check core validation status
        if (coreValidity == false) {
            System.err.println("Signature failed core validation"); 
            boolean sv = signature.getSignatureValue().validate(valContext);
            System.out.println("signature validation status: " + sv);
            // check the validation status of each Reference
            Iterator i = signature.getSignedInfo().getReferences().iterator();
            for (int j = 0; i.hasNext(); j++) {
                boolean refValid = 
                    ((Reference) i.next()).validate(valContext);
                System.out.println("ref[" + j + "] validity status: " + refValid);
            }
        } else {
            System.out.println("Signature passed core validation");
            return true;
        }        
		return false;
	}

	private static byte[] loadSignature(String signatureFile) throws IOException {
		File file = new File(signatureFile);
		FileInputStream fis = new FileInputStream(file);
		byte fileContent[] = new byte[(int)file.length()];
		
		fis.read(fileContent);
		fis.close();
		return fileContent;
	}

	private void saveSignature(String outFile, byte[] signature) throws IOException {
		OutputStream os = new FileOutputStream(outFile);
		os.write(signature);
		os.close();
		System.out.println("Save signed document to "  +outFile);
	}

	private static void installProvider() {
		Security.addProvider(new BouncyCastleProvider());
		
	}	

	private byte[] createEnvelopedSignature(Document docToken) throws Exception {
		
        // First, create a DOM XMLSignatureFactory that will be used to 
        // generate the XMLSignature and marshal it to DOM.
//        String providerName = System.getProperty("jsr105Provider", "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI");
//        
//        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());
		
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");


        // Create a Reference to the enveloped document (in this case we are
        // signing the whole document, so a URI of "" signifies that) and
        // also specify the SHA1 digest algorithm and the ENVELOPED Transform.
        Reference ref = fac.newReference("", 
        		fac.newDigestMethod(DigestMethod.SHA1, null),
             Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), 
             null, null);

        // Create the SignedInfo
        SignedInfo si = fac.newSignedInfo(
        		fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, 
        				(C14NMethodParameterSpec) null), 
        		fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
        		Collections.singletonList(ref));
		
        // Instantiate the document to be signed
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
//        Document doc = dbf.newDocumentBuilder().newDocument();
        Document doc = docToken;
        
        // Load signing key from key-store
        Key signingKey = loadSigningKey(KEYSTORE_FILE, KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEYPASS.toCharArray());
        
		KeyInfoFactory kif = fac.getKeyInfoFactory();
		KeyValue kv = kif.newKeyValue(this.signingCert.getPublicKey());
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv)); 
        
        // Create a DOMSignContext and specify the PrivateKey and
        // location of the resulting XMLSignature's parent element
        DOMSignContext dsc = new DOMSignContext(signingKey, doc.getDocumentElement());

        // Create the XMLSignature (but don't sign it yet)
        XMLSignature signature = fac.newXMLSignature(si, ki);

        // Marshal, generate (and sign) the enveloped signature
        signature.sign(dsc);
        
        // output the resulting document
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.transform(new DOMSource(doc), new StreamResult(os));

		return os.toByteArray();
	}

	private PrivateKey loadSigningKey(String keyStoreFile, char[] keystorePass, String keyAlias, char[] keyPass) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, UnrecoverableEntryException {
		InputStream is = new FileInputStream(keyStoreFile);

		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(is, keystorePass);
		is.close();
		
		PrivateKey privKey = (PrivateKey) keystore.getKey(keyAlias, keyPass);
		signingCert = keystore.getCertificate(keyAlias);
		return privKey;
	}

	   /**
     * KeySelector which retrieves the public key out of the
     * KeyValue element and returns it.
     * NOTE: If the key algorithm doesn't match signature algorithm,
     * then the public key will be ignored.
     */
    private static class KeyValueKeySelector extends KeySelector {
    	
        public KeySelectorResult select(KeyInfo keyInfo,
                                        KeySelector.Purpose purpose,
                                        AlgorithmMethod method,
                                        XMLCryptoContext context)
            throws KeySelectorException {
            if (keyInfo == null) {
                throw new KeySelectorException("Null KeyInfo object!");
            }
            SignatureMethod sm = (SignatureMethod) method;
            List list = keyInfo.getContent();

            for (int i = 0; i < list.size(); i++) {
                XMLStructure xmlStructure = (XMLStructure) list.get(i);
                
                if (xmlStructure instanceof KeyValue) {
                    PublicKey pk = null;
                    try {
                        pk = ((KeyValue)xmlStructure).getPublicKey();
                    } catch (KeyException ke) {
                        throw new KeySelectorException(ke);
                    }
                    // make sure algorithm is compatible with method
                    if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
                        return new SimpleKeySelectorResult(pk);
                    }
                    
                    // make sure the pk found in the trusted public key list.
                    // ...
                }
            }
            throw new KeySelectorException("No KeyValue element found!");
        }

        //@@@FIXME: this should also work for key types other than DSA/RSA
        static boolean algEquals(String algURI, String algName) {
            if (algName.equalsIgnoreCase("DSA") &&
                algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
                return true;
            } else if (algName.equalsIgnoreCase("RSA") &&
                algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
                return true;
            } else if (algName.equalsIgnoreCase("EC") &&
                algURI.equalsIgnoreCase("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256")) {
                return true;
            } else {
                return false;
            }
        }
    }

    private static class SimpleKeySelectorResult implements KeySelectorResult {
        private PublicKey pk;
        SimpleKeySelectorResult(PublicKey pk) {
            this.pk = pk;
        }

        public Key getKey() { return pk; }
    }	
}
