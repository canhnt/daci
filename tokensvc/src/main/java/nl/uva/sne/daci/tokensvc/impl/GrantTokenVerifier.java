package nl.uva.sne.daci.tokensvc.impl;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.security.keys.storage.StorageResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import nl.uva.sne.daci.tokensvc.signature.KeyValueKeySelector;

public class GrantTokenVerifier {
	protected final Logger log = LoggerFactory.getLogger(GrantTokenVerifier.class);
	
	private StorageResolver trustedCertificates;
	
	public GrantTokenVerifier(StorageResolver trustedCertificates) {
		this.trustedCertificates = trustedCertificates;
	}
	/**
	 * Verify the token trust.
	 * 
	 * @param tokenDoc
	 * @return
	 * @throws Exception 
	 */
	public boolean verify(InputStream isToken) throws Exception {
		
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(isToken);
				
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
        DOMValidateContext valContext = new DOMValidateContext(new KeyValueKeySelector(trustedCertificates), nl.item(0));
                
        // unmarshal the XMLSignature
        XMLSignature signature = fac.unmarshalXMLSignature(valContext);        
        
        // Validate the XMLSignature (generated above)
        boolean coreValidity = signature.validate(valContext); 

        // Check core validation status
        if (coreValidity == false) {
            log.error("Signature failed core validation"); 
            boolean sv = signature.getSignatureValue().validate(valContext);
            log.debug("signature validation status: " + sv);
            // check the validation status of each Reference
            Iterator i = signature.getSignedInfo().getReferences().iterator();
            for (int j = 0; i.hasNext(); j++) {
                boolean refValid = 
                    ((Reference) i.next()).validate(valContext);
                log.error("ref[" + j + "] validity status: " + refValid);
            }
        } else {
            log.debug("Signature passed core validation");
            return true;
        }        
        
		return false;
	}

}
