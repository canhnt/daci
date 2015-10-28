/**
 * 
 */
package nl.uva.sne.daci.policygenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author canhnt
 *
 */
public class AttributeTemplate {
	
	
	private OntModel model;

	public AttributeTemplate(String owlFile){
		loadTemplate(owlFile);
	}
	
	private void loadTemplate(String owlFile) {
		model = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM);
		model.read(owlFile, "RDF/XML");		
	}
	
	private List<Attribute> queryAttributes(String queryString, List<String> vars) {
		Query query = QueryFactory.create(queryString);
		
		for(String var:vars)
			query.addResultVar(var);
		
	    QueryExecution qe = QueryExecutionFactory.create(query, model);
	    ResultSet results =  qe.execSelect();
	    
//	    System.out.println(ResultSetFormatter.asText(results));  
	    	   	    
	    List<Attribute> attributes = new ArrayList<Attribute>();
	    
	    while (results.hasNext()) {	    	
	    	QuerySolution qs = results.nextSolution();
	    	
	    	String attrId = qs.get(vars.get(0)).toString();
	    	String attrValue = qs.get(vars.get(1)).toString();
	    	attributes.add(new Attribute(attrId, attrValue));
	    }
	    qe.close();
	    
	    return attributes;
	}

	public List<Attribute> loadVIActions() {
		String viQuery =        
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"PREFIX imf: <http://geysers.eu/imf.owl#>\n" + 
				"PREFIX daci: <http://geysers.eu/imf-daci.owl#>\n" + 
				"SELECT ?id ?value\n" + 
				"WHERE {\n" + 
				"		?vi rdf:type imf:VirtualInfrastructure.\n" + 
				"		?vi daci:hasAttribute ?attr.\n" + 
				"		?attr rdf:type daci:Attribute.\n" + 
				"		?attr daci:hasId ?id.\n" + 
				"		?attr daci:hasValue ?value\n" + 
				"}\n";	
		
		List<String> vars = new ArrayList<String>();
		vars.add("?id");
		vars.add("?value");
		return queryAttributes(viQuery, vars);
	}

	public List<Attribute> loadVRActions() {
		String vrQuery =        
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"PREFIX imf: <http://geysers.eu/imf.owl#>\n" + 
				"PREFIX daci: <http://geysers.eu/imf-daci.owl#>\n" + 
				"SELECT ?id ?value ?r ?type\n" +
				"WHERE {\n" +				
				"		imf:VirtualResource rdfs:subClassOf* ?type.\n" +
				"		?r rdf:type ?type.\n" +
				"		?r daci:hasAttribute ?a.\n" +
				"		?a daci:hasId ?id.\n" +
				"		?a daci:hasValue ?value\n" +				
				"}\n";
		
		List<String> vars = new ArrayList<String>();
		vars.add("?id");
		vars.add("?value");
		return queryAttributes(vrQuery, vars);
	}
	
	public List<Attribute> loadVNodeActions() {
		String vrQuery =        
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"PREFIX imf: <http://geysers.eu/imf.owl#>\n" + 
				"PREFIX daci: <http://geysers.eu/imf-daci.owl#>\n" + 
				"SELECT ?id ?value ?r ?type\n" +
				"WHERE {\n" +				
				"		imf:VirtualNode rdfs:subClassOf* ?type.\n" +
				"		?r rdf:type ?type.\n" +
				"		?r daci:hasAttribute ?a.\n" +
				"		?a daci:hasId ?id.\n" +
				"		?a daci:hasValue ?value\n" +				
				"}\n";
		
		List<String> vars = new ArrayList<String>();
		vars.add("?id");
		vars.add("?value");
		return queryAttributes(vrQuery, vars);
	}	

	
	public  List<Attribute> loadActionAttributes (String resourceType) {
		String vrQuery =        
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"PREFIX imf: <http://geysers.eu/imf.owl#>\n" + 
				"PREFIX daci: <http://geysers.eu/imf-daci.owl#>\n" + 
				"SELECT ?value\n" +
				"WHERE {\n" +				
				        resourceType + " rdfs:subClassOf* ?type.\n" +
				"		?r rdf:type ?type.\n" +
				"		?r daci:hasAttribute ?a.\n" +
				"		?a daci:hasId ?id.\n" +
				"		?a daci:hasValue ?value\n" +				
				"}\n";
		
		List<String> vars = new ArrayList<String>();
		vars.add("?id");
		vars.add("?value");
		return queryAttributes(vrQuery, vars);		
	}
}
