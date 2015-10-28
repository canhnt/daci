package nl.uva.sne.daci.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class BenchmarkClient {
	
	private final AuthzSvcClient[] children;
	
	private final CountDownLatch latch;

	private static final String TENANT_ID_DELIMITER = ";";

	public static final int NUM_TENANTS_PER_THREAD = 10;
	
	public static final int DEFAULT_NUM_THREADS = 1;

	public List<String> lstTenantIds;
	
	public BenchmarkClient(int nNumThread, String authzSvcAddress, String tenantFile) throws Exception {
		
		this.children = new AuthzSvcClient[nNumThread];
        this.latch = new CountDownLatch(children.length);
        
        this.lstTenantIds = loadTenantIds(tenantFile);
        
        
        for(int i = 0; i < children.length; i++) {
			List<String> currentTenantIds = getChildrenTenantIds(i);
			
			children[i] = new AuthzSvcClient(this.latch, currentTenantIds, authzSvcAddress, "Thread " + i);
			children[i].init();
        }
	}

	private List<String> getChildrenTenantIds(int i) {
    	int fromIndex = (i * NUM_TENANTS_PER_THREAD) % lstTenantIds.size();
		int toIndex =  (fromIndex +  NUM_TENANTS_PER_THREAD - 1) % lstTenantIds.size();
		
		List<String> currentTenantIds = new ArrayList<String>(lstTenantIds.subList(fromIndex, toIndex));
		if (toIndex + 1 < lstTenantIds.size())
			currentTenantIds.add(lstTenantIds.get(toIndex+1));
		else
			currentTenantIds.add(lstTenantIds.get(0));
		
		return currentTenantIds;
	}

	private void run() {
		startChildThreads();
        waitForChildThreadsToComplete();

        printStatistics();
	}

	private void printStatistics() {
        long nTotalTime = 0;
        long nTotalRequests = 0;
        
		int numPermitResponses = 0;		
		int numDenyResponses = 0;
		int numNAResponses = 0;
		int numNullRespones = 0;
		int numUnknownResponses = 0;
		int numErrorResponses = 0;
        
        for(int i = 0; i < this.children.length; i++) {
        	nTotalTime += children[i].getExecTime();
        	nTotalRequests += children[i].getTotalRequests();
        	
        	numPermitResponses 	+= children[i].getNumPermitResponses();
        	numDenyResponses 	+= children[i].getNumDenyResponses();
        	numNAResponses 		+= children[i].getNumNAResponses();
        	numNullRespones 	+= children[i].getNumNullRespones();
        	numUnknownResponses += children[i].getNumUnknownResponses();
        	numErrorResponses 	+= children[i].getNumErrorResponses();
        	
        }
		
        long nAverageTimePerThread = nTotalTime / this.children.length;
        
		System.out.println("# Permit requests	:" + numPermitResponses);
		System.out.println("# NA requests	 	:" + numNAResponses);
		System.out.println("# Deny requests		:" + numDenyResponses);
		System.out.println("# Null requests		:" + numNullRespones);
		System.out.println("# Error requests	:" + numErrorResponses);
		System.out.println("# Unknown requests	:" + numUnknownResponses + "\n");		
		
		System.out.println("Total time		: " + nTotalTime + "(ms)");
		System.out.println("Average time/thread	: " + nAverageTimePerThread + "(ms)");
		System.out.println("Total requests		: " + nTotalRequests);
		
		if (nTotalTime > 0)
			System.out.println("Response time (ms/request): " + ((double)nAverageTimePerThread / nTotalRequests));		
	}

	private void waitForChildThreadsToComplete() {
       try {
            latch.await();
            System.out.println("All child threads have completed.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }		
	}

	private void startChildThreads() {
		System.out.println("Starting " + this.children.length + " threads");
		Thread[] threads = new Thread[this.children.length];
		
		for(int i = 0; i < threads.length; i++) {
			AuthzSvcClient child = children[i];
			threads[i] = new Thread(child);
			threads[i].start();
		}
		
	}

	private List<String> loadTenantIds(String tenantIdFile) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(tenantIdFile));
		scanner.useDelimiter(TENANT_ID_DELIMITER);
		
		List<String> tenants = new ArrayList<String>();
		
		while (scanner.hasNext()) {
			tenants.add(scanner.next());
		}
		scanner.close();
		return tenants;
	}
	
	public static void main(String[] args) throws Exception {
		
		int nNumThread = 0;
		String tenantFile = null;
		String authzSvcAddress = null;
		
		if (args.length == 3) {
			nNumThread = Integer.parseInt(args[0]);
			authzSvcAddress = args[1];
			tenantFile = args[2];
		}
		else {
			System.out.println("Syntax: benchmarkclient <num-thread> <remote-address> <tenant-file>");
			return;
		}
		
		BenchmarkClient client = new BenchmarkClient(nNumThread, authzSvcAddress, tenantFile);
		client.run();		
	}
}
