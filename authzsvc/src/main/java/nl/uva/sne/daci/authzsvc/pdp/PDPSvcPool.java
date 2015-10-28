package nl.uva.sne.daci.authzsvc.pdp;

public interface PDPSvcPool {
	PDPSvc getService(String id) throws Exception;
	
//	void addService(String id, PDPSvc svc);
//	
//	void removeService(String id);		
}
