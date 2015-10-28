package nl.uva.sne.daci.context.impl;

import java.util.ArrayList;
import java.util.List;

import nl.uva.sne.daci.context.Context;
import nl.uva.sne.daci.context.ContextStore;

public class ContextStoreImpl implements ContextStore {

	private List<Context> contexts;
	
	private List<Context> rootContexts;
	
	public ContextStoreImpl() {
		contexts = new ArrayList<Context>();
		rootContexts = new ArrayList<Context>();
	}
	
	@Override
	public List<Context> getContexts() {
		return contexts;
	}

	public void add(Context c) {
		contexts.add(c);
	}
	
	public void addRoot(Context c) {
		add(c);
		rootContexts.add(c);
	}


	@Override
	public boolean isRootContext(Context c) {
		return rootContexts.contains(c);
	}
	
}
