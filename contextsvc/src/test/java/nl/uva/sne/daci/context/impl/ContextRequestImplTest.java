package nl.uva.sne.daci.context.impl;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import nl.uva.sne.daci.context.ContextRequest;
import nl.uva.sne.daci.context.ContextRequestBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContextRequestImplTest {

	public static String[][] attributes = new String[][]{
		{"urn:oasis:names:tc:xacml:1.0:action:action-id", "SLI:Operate-VR:Stop"},
		{"urn:oasis:names:tc:xacml:1.0:subject:subject-role", "admin"},
		{"urn:oasis:names:tc:xacml:1.0:resource:resource-id", "http://demo3.uva.nl/vi/745/ComputingNode"}
	};
	
	@Test
	public void test() {
		Map<String, String> request = new HashMap<String, String>();
		for (int i = 0; i < attributes.length; i++) {
			request.put(attributes[i][0], attributes[i][1]);
		}
		
		ContextRequestBuilder builder = new ContextRequestBuilder ();
		ContextRequest ctxRequest = builder.create(request);
		
		System.out.println("ContextRequest:" + ctxRequest.toString());
	}

}
