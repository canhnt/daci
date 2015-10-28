package nl.uva.sne.daci.tokensvc.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.xml.security.keys.storage.StorageResolver;

import nl.uva.sne.daci.tokensvc.certificate.CertificateStore;

public class Configuration {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Configuration.class);
	
	private static Configuration singleton;

	private String keyStoreFile;

	private String keyAlias;

	private String keyPassword;

	private String keyStorePassword;

	private String baseDir;
	
	private String trustedKeyStoreFile;
	
	private String trustedKeyStorePassword;
	
	private X509Certificate signingCert;

	private PrivateKey signingPrivKey;
	
//	private CertificateStore trustedCerts;

	private StorageResolver trustedCertificates;

	


	
	private Configuration(){
		
	}
	
	public static Configuration getInstance() {
		if (singleton == null)
			singleton = new Configuration();
		
		return singleton;
	}

	public void setBaseDir(String path) {
		this.baseDir = path;		
	}

	
	public void setKeyStore(String filename) {
		this.keyStoreFile = filename;
		
	}

	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
		
	}

	public void setKeyStorePassword(String password) {
		this.keyStorePassword = password;
		
	}

	public void setKeyPassword(String password) {
		this.keyPassword = password;		
		
	}

	public void setTrustedKeyStore(String filename) {
		this.trustedKeyStoreFile = filename;
		
	}

	public void setTrustedKeyStorePassword(String password) {
		this.trustedKeyStorePassword = password;		
	}
	

	public void initialize() throws Exception {
        // Load signing key from key-store
		String keystorePath = this.baseDir + this.keyStoreFile;
        try {
			loadSigningKey(keystorePath, this.keyStorePassword.toCharArray(), this.keyAlias, this.keyPassword.toCharArray());
			
			loadTrustedKeyStores();
		} catch (NoSuchAlgorithmException | CertificateException
				| KeyStoreException | UnrecoverableEntryException | IOException e) {
			log.error("Error loading signing keystore at {}", keystorePath);
			throw e;
		}

	}

	private void loadTrustedKeyStores() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
		
		FileInputStream is = new FileInputStream(this.baseDir + this.trustedKeyStoreFile);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(is, this.trustedKeyStorePassword.toCharArray());
		
		this.trustedCertificates = new StorageResolver(ks);
		
		this.trustedCertificates.add(this.signingCert);
	}

	private void loadSigningKey(String keyStoreFile, char[] keystorePass, String keyAlias, char[] keyPass) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, UnrecoverableEntryException {
		InputStream is = new FileInputStream(keyStoreFile);

		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(is, keystorePass);
		is.close();
		
		// Private signing key
		this.signingPrivKey = (PrivateKey) keystore.getKey(keyAlias, keyPass);
		
		// Certificate of the signing key.
		Certificate cert = keystore.getCertificate(keyAlias);
		if (cert instanceof X509Certificate)
			this.signingCert = (X509Certificate) cert;
		else { 
			log.error("Error loading signing certificate: it's not a X509Certificate instance");
			throw new KeyStoreException("Error: signing certificate is not X509Certificate");
		}
	}

	public PrivateKey getSigningKey() {
		return this.signingPrivKey;
	}

	public Certificate getSigningCert() {
		return this.signingCert;
	}
	
	public StorageResolver getTrustedCerts() {
//		return this.trustedCerts;
		return this.trustedCertificates;
	}


}
