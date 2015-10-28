package nl.uva.sne.daci.tokensvc.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Security;

import javax.ws.rs.core.Response;

import nl.uva.sne.daci._1_0.schema.RequestType;
import nl.uva.sne.daci.tokensvc.TokenSvc;
import nl.uva.sne.daci.tokensvc.rest.RESTTokenSvcImpl;
import nl.uva.sne.daci.tokensvc.utils.XMLUtil;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3._2000._09.xmldsig.KeyInfoType;
import org.w3c.dom.Document;

public class TokenSvcImpl implements TokenSvc {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenSvcImpl.class);
	
	protected GrantTokenGenerator grantTokenGenerator;
	
	protected GrantTokenVerifier grantTokenVerifier;
	
	public void init() {
		log.info("Initializing token service");
		
//		String currentDir = System.getProperty("user.dir");
//        System.out.println("Current dir using System:" +currentDir);
        
		installCryptoProvider();
		
		try {
			Configuration.getInstance().initialize();
		} catch (Exception e) {
			error(e.getMessage());
			throw new RuntimeException(e);		
		}
		
		this.grantTokenGenerator = new GrantTokenGenerator(Configuration.getInstance().getSigningKey(),
				Configuration.getInstance().getSigningCert());
		
		this.grantTokenVerifier = new GrantTokenVerifier(Configuration.getInstance().getTrustedCerts());
	}	
	
	private void installCryptoProvider() {
		Security.addProvider(new BouncyCastleProvider());
//		Security.addProvider(new org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI());
	}	
	
	private void error(String message) {
		log.error("Error instantiate token service:\n{}", message);
		
	}

	public void setBaseDir(String path) {
		Configuration.getInstance().setBaseDir(path);
		log.info("Configuration path: {}", path);
	}

	public void setTrustedKeyStore(String filename) {
		Configuration.getInstance().setTrustedKeyStore(filename);
		log.info("Trusted key store filename: {}", filename);
	}
	
	public void setTrustedKeyStorePassword(String password) {
		Configuration.getInstance().setTrustedKeyStorePassword(password);		
	}
	
	public void setKeyStore(String filename) {
		Configuration.getInstance().setKeyStore(filename);
		log.info("Keystore filename: {}", filename);
	}
	
	public void setKeyAlias(String keyAlias) {
		Configuration.getInstance().setKeyAlias(keyAlias);
		log.info("Key alias: {}", keyAlias);
	}
	
	public void setKeyStorePassword(String password) {
		Configuration.getInstance().setKeyStorePassword(password); 
	}
	
	public void setKeyPassword(String password) {
		Configuration.getInstance().setKeyPassword(password);
	}	
	
	@Override
	public String issueGrantToken(String tenantId, RequestType request,
			KeyInfoType userKeyInfo) {
		
		Document signedDoc;
		try {
			signedDoc = grantTokenGenerator.generateAndSign(tenantId, request, userKeyInfo);
			
		} catch (Exception e) {
			log.error("Error signing token: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
		
		try {
			
			byte[] signedToken = XMLUtil.toByteArray(signedDoc);
			String sToken = encodeByteArray(signedToken);			
			log.debug("Return token:{}", sToken);
			
			return sToken;
		} catch (Exception e) {
			log.error("Error converting token from bytes to string");
			return null;
		}
	}

	@Override
	public boolean verifyGrantToken(String token) {
		if (token == null || token.isEmpty()) {
			log.error("Null or empty input token");
			return false;
		}
			
		try {			
			log.debug("Verify grant-token: input string {}", token);
			InputStream istream = new ByteArrayInputStream(URLDecoder.decode(token, "UTF-8").getBytes());
			
			return grantTokenVerifier.verify(istream);
			
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
		return false;
	}

	private static String encodeByteArray(byte[] param) throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder ();
		
		builder.append(URLEncoder.encode(new String(param), "UTF-8"));
		
		return builder.toString();
	}
	
}
