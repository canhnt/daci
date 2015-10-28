package nl.uva.sne.daci.context.impl;

import nl.uva.sne.daci.context.ContextResponse;

public class ContextBaseResponse implements ContextResponse {

	private ContextDecision decision;
	private String grantToken;
	
	public ContextBaseResponse(ContextDecision decision){
		this.decision = decision;
	}
	public ContextBaseResponse(String grantToken){
		this.decision = ContextDecision.NEED_REMOTE_DECISION;
		this.grantToken = grantToken;
	}
	
	public ContextBaseResponse(){
		this.decision = ContextDecision.ERROR;
	}
	
	@Override
	public ContextDecision getDecision() {
		return this.decision;
	}

	@Override
	public String getGrantToken() {
		return this.grantToken;
	}

}
