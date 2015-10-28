package nl.uva.sne.daci.tokensvc;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import nl.uva.sne.daci.tokensvc.impl.GrantTokenVerifier;

import org.apache.xml.security.keys.storage.StorageResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GrantTokenVerifierTest {
	
	private static final String TRUSTED_KEYSTORE_FILE = "src/test/resources/trusted-keystore.jks";
	
	private static final String KEYSTORE_PASSWORD = "trusted";
	
	private static final String SIGNED_TOKEN_FILE = "src/test/resources/grant-token-signature.xml";
		
	GrantTokenVerifier verifier;
	
	@Before
	public void setUp() throws Exception {
		StorageResolver resovler = loadTrustedKeyStores();
				
		verifier = new GrantTokenVerifier(resovler);				
	}
	
	private StorageResolver loadTrustedKeyStores() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
		
		FileInputStream is = new FileInputStream(TRUSTED_KEYSTORE_FILE);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(is, KEYSTORE_PASSWORD.toCharArray());
		
        StorageResolver resovler = new StorageResolver(ks);
		
		return resovler;
	}


	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testVerify() throws Exception {
		
		InputStream is = new FileInputStream(SIGNED_TOKEN_FILE);
		
		assertTrue(verifier.verify(is));
		is.close();
	}

}
