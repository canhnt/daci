package nl.uva.sne.daci.authzsvc;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import nl.uva.sne.daci.authzsvc.AuthzSvc.DecisionType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuthzResponseTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws JAXBException {
		 JAXBContext jc = JAXBContext.newInstance(AuthzResponse.class);
		 
		 AuthzResponse resp = new AuthzResponse(DecisionType.PERMIT, "token123");
		 
		 String s = marshalResponse(jc, resp);
		 
		 System.out.println(s);
		 
		 //unmarshal
		 AuthzResponse r2 = unmarshalResponse(jc, s);
		 assertNotNull(r2);
	}


	public static AuthzResponse unmarshalResponse(JAXBContext jaxbContext, String value) throws JAXBException {
		StringReader r = new StringReader(value); 
		Unmarshaller um = jaxbContext.createUnmarshaller();
		return (AuthzResponse) um.unmarshal(r);
	}

	public static String marshalResponse(JAXBContext jaxbContext, AuthzResponse resp) throws JAXBException {
		StringWriter writerTo = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 
	    marshaller.marshal(resp, writerTo); //create xml string from the input object
	    
		return writerTo.toString();
	}

}
