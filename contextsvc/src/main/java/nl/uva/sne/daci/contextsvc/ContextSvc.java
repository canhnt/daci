package nl.uva.sne.daci.contextsvc;

import nl.uva.sne.daci.context.ContextRequest;
import nl.uva.sne.daci.context.ContextResponse;

public interface ContextSvc {
	ContextResponse validate(ContextRequest request) throws Exception;
}
