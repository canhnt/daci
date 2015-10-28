package nl.uva.sne.daci.policygenerator.vi;

public class Resource {
	private String id;
	private String type;
	
	public Resource(String id, String type) {
		this.id = id;
		this.type = type;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getType() {
		return this.type;
	}
}
