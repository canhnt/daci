package nl.uva.sne.daci.authzsvc;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuthzRequestTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws JAXBException {
		 JAXBContext jc = JAXBContext.newInstance(AuthzRequest.class);
		 
		 AuthzRequest request = new AuthzRequest();
		 Map<String, String> attrs = new HashMap<String, String>();
		 attrs.put("subject-id", "Canh");
		 attrs.put("resource-id", "VI-2");
		 attrs.put("action-id", "Deploy");
		 request.setAttributes(attrs);
		 
		 String s = marshalRequest(jc, request);
		 
		 System.out.println(s);
		 
		 //unmarshal
		 AuthzRequest r2 = unmarshalRequest(jc, s);
		 assertNotNull(r2);
	}

	public static AuthzRequest unmarshalRequest(JAXBContext jaxbContext, String s) throws JAXBException {
		StringReader r = new StringReader(s); 
		Unmarshaller um = jaxbContext.createUnmarshaller();
		return (AuthzRequest) um.unmarshal(r);
	}

	public static String marshalRequest(JAXBContext jaxbContext, AuthzRequest request) throws JAXBException {
		StringWriter writerTo = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 
	    marshaller.marshal(request, writerTo); //create xml string from the input object
	    
		return writerTo.toString();
	}

}
