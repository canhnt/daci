package nl.uva.sne.daci.tokensvc.certificate;

import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.dsig.keyinfo.X509Data;

import org.apache.xml.security.keys.storage.StorageResolver;

public class CertificateStore {
		List<X509Certificate> certs;
		
		public CertificateStore(List<X509Certificate> certificates) {
			certs = new ArrayList<X509Certificate>(certificates.size());
			certs.addAll(certificates);
		}
		
		/**
		 * Load list of certificates from a stream
		 * @param is
		 */
		public CertificateStore(InputStream is) {
		 
		}
		
		public void add(X509Certificate cert) {
			this.certs.add(cert);
		}
		
		public void remove(X509Certificate cert) {
			this.certs.remove(cert);
		}
		
		public boolean contains(X509Certificate cert) {
			return this.certs.contains(cert);
		}
		
		/**
		 * Return true if found the public key in the store.
		 * 
		 * @param pubKey
		 * @return
		 */
		public boolean findPublicKey(PublicKey pubKey) {
			return false;
		}
		
		/**
		 * Return true if the X509Data object found in the store
		 * @param x509Data
		 * @return
		 */
		public boolean findX509(X509Data x509Data){
			return false;
		}
		
}
