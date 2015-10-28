package nl.uva.sne.daci.policygenerator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import nl.uva.sne.semantic.semcore.TripleOutputStreamWriter;
import nl.uva.sne.xacml.util.XACMLUtil;

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

import eu.geysers.bundles.licl.imf.create.model.resource.IMFVirtualInfrastructure;
import eu.geysers.bundles.licl.imf.create.utils.IMFVIRequestGenerator;
import eu.geysers.bundles.licl.imf.create.utils.dictionary.IMFDictionary;

public class LoadVI {

	public static final String VI_DESC_FILE = "src/main/resources/vi-sample1.xml";
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
			
//		genPolicy("src/main/resources/vi-sample1k.xml", 
//				  "src/main/resources/vi-sample1k-provider-policies.xml", 
//				  "src/main/resources/vi-sample1k-intertenant-policies.xml",
//				  "src/main/resources/vi-sample1k-intratenant-policies.xml");

		genPolicy("src/main/resources/vi-sample800.xml", 
				  "src/main/resources/vi-sample800-provider-policies.xml", 
				  "src/main/resources/vi-sample800-intertenant-policies.xml",
				  "src/main/resources/vi-sample800-intratenant-policies.xml");		
	}
	
	private static void genPolicy(String inVIDescFile, 
			String outProvPolicyFile, 
			String outInterTenantPolicyFile, 
			String outIntraTenantPolicyFile) throws FileNotFoundException {
		OntModel viModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);		
		viModel.read(inVIDescFile, "RDF/XML" );
		
		AttributeTemplate at = new AttributeTemplate("src/main/resources/imf-ac.owl");
		
		PolicyGenerator pg = new PolicyGenerator(viModel, at);
		
		// generate delegation policies from provider -> tenants.
		PolicySetType ps = pg.generateProviderPolicies();
			
		OutputStream policyOut = new FileOutputStream(outProvPolicyFile);
		XACMLUtil.print(new ObjectFactory().createPolicySet(ps), PolicySetType.class, policyOut);

		// Generate sharing policies between tenants.
		PolicySetType interTenantPS = pg.generateInterTenantPolicies();		
		OutputStream interTenantPolicyOut = new FileOutputStream(outInterTenantPolicyFile);
		XACMLUtil.print(new ObjectFactory().createPolicySet(interTenantPS), PolicySetType.class, interTenantPolicyOut);
		
		// Generate intra-tenant policies for each tenant.
		PolicySetType intraTenantPS = pg.generateIntraTenantPolicies();		
		OutputStream intraTenantPolicyOut = new FileOutputStream(outIntraTenantPolicyFile);
		XACMLUtil.print(new ObjectFactory().createPolicySet(intraTenantPS), PolicySetType.class, intraTenantPolicyOut);		
		
	}

	public static void loadActions() {
		AttributeTemplate at = new AttributeTemplate("src/main/resources/imf-ac.owl");
		
//		System.out.println("VI actions:");
//		List<Attribute> viActions = at.loadVIActions();		
//		for(Attribute a:viActions) {
//			System.out.println("attribute-id:" + a.getId()+ "\t value:" + a.getValue());
//		}
		
		System.out.println("VirtualNode actions:");
		List<Attribute> vrActions = at.loadVNodeActions();		
		for(Attribute a:vrActions) {
			System.out.println(a.getId()+ "\t :" + a.getValue());
		}
	}
	
	private static void queryRDF() {
		OntModel model1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		
		model1.read("src/main/resources/vi-sample1.xml", "RDF/XML" );
		 
		String queryString =        
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"PREFIX imf: <http://geysers.eu/imf.owl#>\n" +
				"SELECT ?vi ?r ?rtype \n" +
				"WHERE {\n" +
				"	?vi rdf:type imf:VirtualInfrastructure.\n" +
				"	?vi imf:hasResource ?r.\n" +
				"	?r rdf:type ?rtype.\n" +
				"	?rtype rdfs:subClassOf* imf:Resource.\n" +
				"}";
		
		Query query = QueryFactory.create(queryString);
		
	    QueryExecution qe = QueryExecutionFactory.create(query, model1);
	    ResultSet results =  qe.execSelect();
	    System.out.println(ResultSetFormatter.asText(results));	    	    
	    qe.close();
	}
	
	

	public static void generateVI() throws Exception {
		IMFVirtualInfrastructure vi = IMFVIRequestGenerator.generateLinkSpecificRequest("http://demo3.uva.nl/vi/1/");
		
		OutputStream viOut = new FileOutputStream("src/main/resources/vi-request.xml");
		TripleOutputStreamWriter writer = new TripleOutputStreamWriter(new IMFDictionary());
		writer.writeOWL(vi, true, viOut);
		
	}
	
	private void write(JAXBElement<PolicyType> jaxbElement) {
        try {
			JAXBContext jc = JAXBContext.newInstance(PolicyType.class);
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			m.marshal(jaxbElement, System.out);
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
