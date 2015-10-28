package nl.uva.sne.daci.authzsvc;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="AuthzRequest", namespace="http://sne.uva.nl/daci/authzsvc")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class AuthzRequest {

	private Map<String, String> attributes;
		
	public Map<String, String> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	public String getAttribute(String id) {
		return attributes.get(id);
	}

	public void updateAttribute(String id, String value) {
		attributes.put(id, value);
		
	}

	public void deleteAttribute(String id, String value) {
		attributes.remove(id);
	}

	public void addAttributes(Map<String, String> attributes) {
		this.attributes.putAll(attributes);		
	}

}
