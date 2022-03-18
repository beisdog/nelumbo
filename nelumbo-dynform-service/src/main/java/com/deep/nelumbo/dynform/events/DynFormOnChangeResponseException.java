package com.deep.nelumbo.dynform.events;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class DynFormOnChangeResponseException extends HttpStatusCodeException {

    private DynFormOnChangeResponse response;

    public DynFormOnChangeResponseException(HttpStatus code, DynFormOnChangeResponse response) {
        super(code);
        this.response = response;
    }

    public DynFormOnChangeResponse getDynFormOnChangeResponse() {
        return response;
    }

    public void setDynFormOnChangeResponse(DynFormOnChangeResponse response) {
        this.response = response;
    }

}
