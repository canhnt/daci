package nl.uva.sne.daci.tokensvc;

import org.w3._2000._09.xmldsig.KeyInfoType;

import nl.uva.sne.daci._1_0.schema.RequestType;


public interface TokenSvc {
	
	String issueGrantToken(String tenantId, RequestType request, KeyInfoType userKeyInfo);
		
	boolean verifyGrantToken(String token);
}
