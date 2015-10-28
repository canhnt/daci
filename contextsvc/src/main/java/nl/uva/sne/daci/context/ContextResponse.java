package nl.uva.sne.daci.context;

public interface ContextResponse {
	enum ContextDecision {
		PERMIT,
		DENY,
		NEED_REMOTE_DECISION,
		ERROR
	}
	
	ContextDecision getDecision();
	
	/**
	 * Return the grant-token for the remote access to resource in other domain.
	 * @return
	 */
	String getGrantToken();
}
