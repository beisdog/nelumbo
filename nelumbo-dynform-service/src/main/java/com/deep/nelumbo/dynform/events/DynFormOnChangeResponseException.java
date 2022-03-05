package com.deep.nelumbo.dynform.events;

public class DynFormOnChangeResponseException extends javax.ws.rs.BadRequestException{
	
	private static final long serialVersionUID = 1L;
	
	private DynFormOnChangeResponse response;

	public DynFormOnChangeResponseException(DynFormOnChangeResponse response) {
		super();
		this.response = response;
	}

	public DynFormOnChangeResponse getDynFormOnChangeResponse() {
		return response;
	}

	public void setDynFormOnChangeResponse(DynFormOnChangeResponse response) {
		this.response = response;
	}

}
