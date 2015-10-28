package nl.uva.sne.daci.tokensvc.signature;

import java.security.KeyException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

import nl.uva.sne.daci.tokensvc.impl.GrantTokenVerifier;

import org.apache.xml.security.keys.storage.StorageResolver;
import org.bouncycastle.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyValueKeySelector extends KeySelector {
	protected final Logger log = LoggerFactory.getLogger(KeyValueKeySelector.class);
	
	StorageResolver trustCerts;
	
	public KeyValueKeySelector(StorageResolver trustCerts){
		this.trustCerts = trustCerts;
	}
	
	@Override
	public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose,
			AlgorithmMethod method, XMLCryptoContext context)
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

                	// make sure the pk found in the trusted public key list.
                    // ...
                	if (findPublicKey(pk))
        	         	return new SimpleKeySelectorResult(pk);
                }                
            }
        }
        throw new KeySelectorException("No KeyValue element found!");		
	}

    private boolean findPublicKey(PublicKey pk) {
    	Iterator<Certificate> it = this.trustCerts.getIterator();
    	
    	while (it.hasNext()) {
    		Certificate cert = it.next();
    		PublicKey currentPk = cert.getPublicKey();
    		if (currentPk.getAlgorithm().equals(pk.getAlgorithm()) && 
    			Arrays.areEqual(currentPk.getEncoded(), pk.getEncoded()))
    			log.debug("Found the public key in the trusted list");
    			return true; 
    	}
    	
    	// not found public key in the trusted certificate list
    	return false;
	}

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
