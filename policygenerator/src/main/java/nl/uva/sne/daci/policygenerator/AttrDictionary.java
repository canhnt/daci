package nl.uva.sne.daci.policygenerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttrDictionary {

	public enum ResourceType {
		VI,
		VR,
		VNODE 		
	}
	
	
	public static final Map<ResourceType, List<String>> actionMap = initActionMap();
	
	private static final String[] VI_ACTIONS = {
		"MLI:Request-VI",
		"MLI:Instantiate-VI",
		"MLI:Decomission-VI",
		"MLI:ReplanningVI:Add-VR-IT",
		"MLI:ReplanningVI:Modify-VR-IT",
		"MLI:ReplanningVI:Delete-VR-IT",
		"MLI:ReplanningVI:Add-VLink",
		"MLI:ReplanningVI:Modify-VLink",
		"MLI:ReplanningVI:Delete-VLink",
		"MLI:ReplanningVI:Modify-Time"
	};
	
	private static final String[] VR_ACTIONS = {
		"SLI:Decommision-VR",
		"SLI:Monitor-VR-Power",
		"SLI:Monitor-VR-States",
		"SLI:Monitor-VR-Status",
		"SLI:Operate-VR",
		"SLI:Subscribe-VR-Monitoring",
		"SLI:Unsubscribe-VR-Monitoring"
	};
	
	private static final String[] VNODE_ACTIONS = {
		"SLI:Add-VirtualNetworkIf",
		"SLI:Create-StorageImage",
		"SLI:Remove-StorageImage",
		"SLI:Remove-VirtualNetworkIf"		 
	};
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


	private static Map<ResourceType, List<String>> initActionMap() {
		HashMap<ResourceType, List<String>> map = new HashMap<ResourceType, List<String>>();
		
		map.put(ResourceType.VI, Arrays.asList(VI_ACTIONS));
		map.put(ResourceType.VI, Arrays.asList(VR_ACTIONS));
		map.put(ResourceType.VI, Arrays.asList(VNODE_ACTIONS));
		
		return map;
	}
}
