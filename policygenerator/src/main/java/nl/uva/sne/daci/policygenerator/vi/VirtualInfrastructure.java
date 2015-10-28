package nl.uva.sne.daci.policygenerator.vi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VirtualInfrastructure {
	private List<Resource> resources;
	private String id;
	
	public VirtualInfrastructure(String id) {
		this.id = id;
		resources = new ArrayList<Resource>();
	}
	
	public String getId() {
		return id;
	}
	
	public void addResource(Resource r) {
		resources.add(r);
	}
	
	public List<Resource> getResources(){
		return resources;
	}
}
