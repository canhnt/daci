package nl.uva.sne.daci.tokensvc.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Security;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import nl.uva.sne.daci._1_0.schema.GrantTokenType;
import nl.uva.sne.daci._1_0.schema.RequestType;
import nl.uva.sne.daci.tokensvc.impl.Configuration;
import nl.uva.sne.daci.tokensvc.impl.GrantTokenGenerator;
import nl.uva.sne.daci.tokensvc.impl.GrantTokenVerifier;
import nl.uva.sne.daci.tokensvc.impl.TokenSvcImpl;
import nl.uva.sne.daci.tokensvc.utils.XMLUtil;

import org.w3._2000._09.xmldsig.KeyInfoType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class RESTTokenSvcImpl extends TokenSvcImpl implements RESTTokenSvc {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RESTTokenSvcImpl.class);
	
	public void init() {
		super.init();
	}

	public void setBaseDir(String path) {
		super.setBaseDir(path);
	}

	public void setTrustedKeyStore(String filename) {
		super.setTrustedKeyStore(filename);
	}
	
	public void setTrustedKeyStorePassword(String password) {
		super.setTrustedKeyStorePassword(password);		
	}
	
	public void setKeyStore(String filename) {
		super.setKeyStore(filename);
	}
	
	public void setKeyAlias(String keyAlias) {
		super.setKeyAlias(keyAlias);
	}
	
	public void setKeyStorePassword(String password) {
		super.setKeyStorePassword(password); 
	}
	
	public void setKeyPassword(String password) {
		super.setKeyPassword(password);
	}	
	
	@Override
	public Response issueGrantToken(String tenantId, String request, String userKeyInfo){
		if (tenantId == null || tenantId.isEmpty())
			throw new IllegalArgumentException("tenantId parameter must not be null");

		if (request == null || request.isEmpty())
			throw new IllegalArgumentException("request parameter must not be null");
		
		if (userKeyInfo == null || userKeyInfo.isEmpty())
			throw new IllegalArgumentException("userKeyInfo parameter must not be null");		

		RequestType requestObject;
		try {
			requestObject = convertRequest(request);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error("Error converting request:" + e.getMessage());
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		
		KeyInfoType keyInfoObj;
		try {
			keyInfoObj = convertKeyInfo(userKeyInfo);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error("Error converting keyinfo:" + e.getMessage());
			return Response.status(Response.Status.BAD_REQUEST).build();
		}		
		
		
		try {						
			String sToken = issueGrantToken(tenantId, requestObject, keyInfoObj);						
			
			return Response.ok(sToken).build();
		} catch (Exception e) {
			log.error("Error converting token from bytes to string");
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	private RequestType convertRequest(String request) throws ParserConfigurationException, SAXException, IOException {
		InputStream istream = new ByteArrayInputStream(request.getBytes("UTF-8"));
		
		return XMLUtil.unmarshal(RequestType.class, istream);
	}
	
	private KeyInfoType convertKeyInfo(String keyinfo) throws ParserConfigurationException, SAXException, IOException {
		InputStream istream = new ByteArrayInputStream(keyinfo.getBytes("UTF-8"));
						
		return XMLUtil.unmarshal(KeyInfoType.class, istream);
	}
}
