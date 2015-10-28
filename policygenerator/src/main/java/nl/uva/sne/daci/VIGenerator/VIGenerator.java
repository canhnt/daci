package nl.uva.sne.daci.VIGenerator;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import nl.uva.sne.semantic.semcore.TopOntologyConcept;
import nl.uva.sne.semantic.semcore.TripleOutputStreamWriter;

import eu.geysers.bundles.licl.imf.create.model.resource.IMFNodeComponent;
import eu.geysers.bundles.licl.imf.create.model.resource.IMFResource;
import eu.geysers.bundles.licl.imf.create.model.resource.IMFVirtualInfrastructure;
import eu.geysers.bundles.licl.imf.create.model.resource.IMFVirtualResource;
import eu.geysers.bundles.licl.imf.create.model.resource.IMFResourcePool;
import eu.geysers.bundles.licl.imf.create.model.resource.component.IMFMemoryComponent;
import eu.geysers.bundles.licl.imf.create.model.resource.component.IMFProcessingComponent;
import eu.geysers.bundles.licl.imf.create.model.resource.component.IMFStorageComponent;
import eu.geysers.bundles.licl.imf.create.model.resource.network.IMFInterface;
import eu.geysers.bundles.licl.imf.create.model.resource.network.IMFLink;
import eu.geysers.bundles.licl.imf.create.model.value.IMFValueUnitPair;
import eu.geysers.bundles.licl.imf.create.model.value.enumval.IMFConstant;
import eu.geysers.bundles.licl.imf.create.model.value.enumval.IMFResourceKind;
import eu.geysers.bundles.licl.imf.create.utils.IMFConnect;
import eu.geysers.bundles.licl.imf.create.utils.IMFVIRequestGenerator;
import eu.geysers.bundles.licl.imf.create.utils.dictionary.IMFDictionary;

/**
 * Generate 1000 different VI, store to xml files
 * @author canhnt
 *
 */
public class VIGenerator {
	private static final int MAX_VI = 800;
	private static final String OUT_XML_FILE = "src/main/resources/vi-sample800.xml";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new VIGenerator().generate();

	}
	
	Random rnd;
	List<IMFVirtualInfrastructure> listVIs;
	public VIGenerator(){
		rnd = new Random();
	}
	
	public void generate() {
		listVIs = new ArrayList<IMFVirtualInfrastructure>();
		
		for(int i = 0; i < MAX_VI; i++) {
			listVIs.add(generateVI("http://demo3.uva.nl/vi/" + i + "/" ));
		}		
		
		saveVIs(OUT_XML_FILE);
		
	}
	
	private void saveVIs(String outFile) {
		List<TopOntologyConcept> result = new ArrayList<TopOntologyConcept>();
		for(IMFVirtualInfrastructure vi:listVIs) {
			result.add(vi);
		}		
		
		try {
			OutputStream viOut;
			viOut = new FileOutputStream(outFile);
			TripleOutputStreamWriter writer = new TripleOutputStreamWriter(new IMFDictionary());
			writer.writeOWL(result, true, viOut);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private IMFVirtualInfrastructure generateVI(String viGRI){
		int type = rnd.nextInt(3);
		
		switch(type) {
		case 0:
			return generateVIType0(viGRI);
		case 1:
			return generateVIType1(viGRI);
		case 2:
			return generateVIType2(viGRI);
		}
		throw new RuntimeException("Unknown VI template type");
	}

	private IMFVirtualInfrastructure generateVIType0(String viGRI) {
		
		return IMFVIRequestGenerator.generateLinkSpecificRequest(viGRI);
	}

	private IMFVirtualInfrastructure generateVIType1(String viGRI) {
		IMFVirtualInfrastructure vi = new IMFVirtualInfrastructure(viGRI);
		
		IMFVirtualResource storage1 = createStorageNode(viGRI, 1);
		IMFInterface inStorage1 = new IMFInterface(viGRI + "StorageIn1");
		IMFInterface outStorage1 = new IMFInterface(viGRI + "StorageOut1");

		IMFVirtualResource storage2 = createStorageNode(viGRI, 2);
		IMFInterface inStorage2 = new IMFInterface(viGRI + "StorageIn2");
		IMFInterface outStorage2 = new IMFInterface(viGRI + "StorageOut2");

		
		// Create the Computing node with 1GB RAM and 2GHz of computing power
		IMFVirtualResource computing = createComputingNode(viGRI, 1);
		IMFInterface inComputing1 = new IMFInterface(viGRI + "Computing1In");
		IMFInterface outComputing1 = new IMFInterface(viGRI + "Computing1Out");
		IMFInterface inComputing2 = new IMFInterface(viGRI + "Computing2In");
		IMFInterface outComputing2 = new IMFInterface(viGRI + "Computing2Out");
				
				
		// Create links
		IMFLink link1out = new IMFLink(viGRI + "Link1Out");
		link1out.setCapacity(new IMFValueUnitPair(viGRI + "Link1OutCapacity", 100., IMFConstant.Mbit_s));
		IMFLink link1in = new IMFLink(viGRI + "Link1In");
		link1in.setCapacity(new IMFValueUnitPair(viGRI + "Link1InCapacity", 100., IMFConstant.Mbit_s));

		IMFLink link2out = new IMFLink(viGRI + "Link1Out");
		link1out.setCapacity(new IMFValueUnitPair(viGRI + "Link1OutCapacity", 100., IMFConstant.Mbit_s));
		IMFLink link2in = new IMFLink(viGRI + "Link1In");
		link1in.setCapacity(new IMFValueUnitPair(viGRI + "Link1InCapacity", 100., IMFConstant.Mbit_s));
		
		// link1 between storage1 and computing
		IMFConnect.connect(storage1, outStorage1 , link1out, inComputing1, computing);
		IMFConnect.connect(storage1, inStorage1 , link1in, outComputing1, computing);

		// link2 between storage and computing2
		IMFConnect.connect(storage2,outStorage2 , link2out, inComputing2, computing);
		IMFConnect.connect(storage2,inStorage2, link2in, outComputing2, computing);
		
		vi.setResources(Arrays
				.asList((IMFResource) computing, storage1, storage2));

		return vi;		
		
	}
	
	private IMFVirtualInfrastructure generateVIType2(String viGRI) {
		IMFVirtualInfrastructure vi = new IMFVirtualInfrastructure(viGRI);
	
		IMFVirtualResource storage = createStorageNode(viGRI, 1);
		IMFInterface inStorage1 = new IMFInterface(viGRI + "StorageIn1");
		IMFInterface outStorage1 = new IMFInterface(viGRI + "StorageOut1");
		IMFInterface inStorage2 = new IMFInterface(viGRI + "StorageIn2");
		IMFInterface outStorage2 = new IMFInterface(viGRI + "StorageOut2");
		
		
		// Create the Computing node with 1GB RAM and 2GHz of computing power
		IMFVirtualResource computing1 = createComputingNode(viGRI, 1);
		IMFInterface inComputing1 = new IMFInterface(viGRI + "Computing1In");
		IMFInterface outComputing1 = new IMFInterface(viGRI + "Computing1Out");
				
		IMFVirtualResource computing2 = createComputingNode(viGRI, 2);
		IMFInterface inComputing2 = new IMFInterface(viGRI + "Computing2In");
		IMFInterface outComputing2 = new IMFInterface(viGRI + "Computing2Out");
		
		IMFResourcePool rp = createResourcePool(viGRI);
				
		// Create links
		IMFLink link1out = new IMFLink(viGRI + "Link1Out");
		link1out.setCapacity(new IMFValueUnitPair(viGRI + "Link1OutCapacity", 100., IMFConstant.Mbit_s));
		IMFLink link1in = new IMFLink(viGRI + "Link1In");
		link1in.setCapacity(new IMFValueUnitPair(viGRI + "Link1InCapacity", 100., IMFConstant.Mbit_s));

		IMFLink link2out = new IMFLink(viGRI + "Link1Out");
		link1out.setCapacity(new IMFValueUnitPair(viGRI + "Link1OutCapacity", 100., IMFConstant.Mbit_s));
		IMFLink link2in = new IMFLink(viGRI + "Link1In");
		link1in.setCapacity(new IMFValueUnitPair(viGRI + "Link1InCapacity", 100., IMFConstant.Mbit_s));
		
		// link1 between storage and computing1
		IMFConnect.connect(storage,outStorage1 , link1out, inComputing1, computing1);
		IMFConnect.connect(storage,inStorage1 , link1in, outComputing1, computing1);

		// link2 between storage and computing2
		IMFConnect.connect(storage,outStorage2 , link2out, inComputing2, computing2);
		IMFConnect.connect(storage,inStorage2, link2in, outComputing2, computing2);
		
		vi.setResources(Arrays
				.asList((IMFResource) computing1, computing2, storage, rp));

		return vi;
		
	}

	private IMFVirtualResource createStorageNode(String viGRI, int i) {
		IMFVirtualResource storage = new IMFVirtualResource(viGRI + "StorageNode" + i);
		
		storage.setResourceKind(Arrays.asList(IMFResourceKind.STORAGE));
		IMFStorageComponent storageComponent = new IMFStorageComponent(
				viGRI + "StorageNodeComponent");
		storageComponent.setStorageSize(new IMFValueUnitPair(viGRI
				+ "StorageSize", 100. * rnd.nextInt(12), IMFConstant.GB));
		storage.setNodeComponent(Arrays.asList((IMFNodeComponent) storageComponent));
		return storage;
	}

	private IMFVirtualResource createComputingNode(String viGRI, int i) {
		
		IMFVirtualResource computing = new IMFVirtualResource(viGRI + "ComputingNode" + i);
		computing.setResourceKind(Arrays.asList(IMFResourceKind.COMPUTING));
		IMFMemoryComponent memoryComponent = new IMFMemoryComponent(viGRI
				+ "MemoryComponent", new IMFValueUnitPair(viGRI
				+ "MemorySize", 1., IMFConstant.GB));
		IMFProcessingComponent cpu = new IMFProcessingComponent(viGRI
				+ "ProcessingComponent");
		cpu.setCores(1);
		cpu.setProcessorSpeed(new IMFValueUnitPair(viGRI + "CPUSpeed", 2.,
				IMFConstant.GHz));
		List<IMFNodeComponent> computingComponents = new ArrayList<IMFNodeComponent>();
		computingComponents.add((IMFNodeComponent) cpu);
		computingComponents.add((IMFNodeComponent) memoryComponent);
		computing.setNodeComponent(computingComponents);
		return computing;
	}


	private IMFResourcePool createResourcePool(String viGRI) {
		IMFResourcePool rp = new IMFResourcePool(viGRI + "/ResourcePool", rnd.nextInt(100));
		
		IMFStorageComponent storageComponent = new IMFStorageComponent(
				viGRI + "StorageNodeComponent");		
		storageComponent.setStorageSize(new IMFValueUnitPair(viGRI
				+ "StorageSize", 100. * rnd.nextInt(10), IMFConstant.GB));
				
		rp.setNodeComponent(Arrays.asList((IMFNodeComponent) storageComponent));
		
		IMFMemoryComponent memoryComponent = new IMFMemoryComponent(viGRI + "MemoryComponent", 
				new IMFValueUnitPair(viGRI	+ "MemorySize", 16. * rnd.nextInt(4), IMFConstant.GB));
		IMFProcessingComponent cpu = new IMFProcessingComponent(viGRI + "ProcessingComponent");
		cpu.setCores(16 * rnd.nextInt(5));
		
		cpu.setProcessorSpeed(new IMFValueUnitPair(viGRI + "CPUSpeed", 1024. * rnd.nextInt(4)  ,IMFConstant.MHz));
		
		List<IMFNodeComponent> computingComponents = new ArrayList<IMFNodeComponent>();
		computingComponents.add((IMFNodeComponent) cpu);
		computingComponents.add((IMFNodeComponent) memoryComponent);
		
		rp.setNodeComponent(computingComponents);	
		
		return rp;
	}

}
