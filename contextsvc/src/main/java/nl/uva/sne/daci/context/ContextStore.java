package nl.uva.sne.daci.context;

import java.util.List;

public interface ContextStore {

	List<Context> getContexts();
	
	/**
	 * Return true if this is the trusted root context.
	 * 
	 * @param c
	 * @return
	 */
	boolean isRootContext(Context c);

}
