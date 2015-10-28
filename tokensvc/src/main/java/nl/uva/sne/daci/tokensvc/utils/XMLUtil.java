package nl.uva.sne.daci.tokensvc.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XMLUtil {
	
//	public static GrantTokenType unmarshalGrantToken(InputStream is) throws JAXBException{
//		JAXBContext jc = JAXBContext.newInstance(GrantTokenType.class);
//		
//		Unmarshaller unmarshaller = jc.createUnmarshaller();
//		JAXBElement<GrantTokenType> je = (JAXBElement<GrantTokenType>) unmarshaller.unmarshal(is);
//		return je.getValue();
//	}
	
	public static <T> T unmarshal(Class<T> cls, InputStream is) throws ParserConfigurationException, SAXException, IOException {
		Document doc = readXML(is);
		Element xmlDom = doc.getDocumentElement();
		
		return unmarshal(cls, xmlDom);
	}
	
	public static <T> T unmarshal(Class<T> cls, Element dom) {

		try {
			JAXBContext jc = JAXBContext.newInstance(cls);
			Unmarshaller unmarshaller = jc.createUnmarshaller();

			JAXBElement<T> jaxbObject = unmarshaller.unmarshal(dom, cls);

			return jaxbObject.getValue();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Document readXML(InputStream istream)
			throws ParserConfigurationException, SAXException, IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
				.newInstance();
		dbf.setNamespaceAware(true);

		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();

		org.w3c.dom.Document doc = db.parse(istream);

		return doc;
	}

	public static Document readXML(String xmlFile)
			throws ParserConfigurationException, SAXException, IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
				.newInstance();
		dbf.setNamespaceAware(true);

		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();

		org.w3c.dom.Document doc = db.parse(new FileInputStream(xmlFile));

		return doc;
	}
	
	public static <T> void print(JAXBElement<T> jaxbElement, Class<T> cls,
			OutputStream os) {
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(cls);
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			m.marshal(jaxbElement, os);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public static <T> void print(JAXBElement<T> jaxbElement, Class<T> cls, Document doc) {
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(cls);
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			m.marshal(jaxbElement, doc);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	
	public static byte[] toByteArray(Document doc) throws Exception {
		
        // output the resulting document
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.transform(new DOMSource(doc), new StreamResult(os));        
		return os.toByteArray();
		
	}	
}
