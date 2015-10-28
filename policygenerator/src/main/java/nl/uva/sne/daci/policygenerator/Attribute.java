package nl.uva.sne.daci.policygenerator;

public class Attribute {
	
	private String id;
	private String value;
	
	public Attribute(String id, String value){
		this.id = id;
		this.value = value;
	}
	
	public String getId(){
		return this.id;
	}
	public String getValue(){
		return this.value;
	}
}
