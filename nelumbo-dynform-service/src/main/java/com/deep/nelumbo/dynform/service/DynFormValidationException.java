package com.deep.nelumbo.dynform.service;

import com.deep.nelumbo.dynform.dto.DynFormDataDTO;
import com.deep.nelumbo.dynform.dto.FieldMessage;
import com.deep.nelumbo.dynform.dto.Message;

import java.util.List;


public class DynFormValidationException extends RuntimeException {

    private static final long serialVersionUID = -1614133285557275501L;

    private DynFormDataDTO formData;
    private List<FieldMessage> formFieldErrors;

    public DynFormValidationException(DynFormDataDTO data, List<FieldMessage> fieldErrors) {
        super(convertListToErrorMessage(fieldErrors));
        this.formData = data;
        this.formFieldErrors = fieldErrors;
    }

    public static String convertListToErrorMessage(List<FieldMessage> list) {
        StringBuilder builder = new StringBuilder();

        for (Message error : list) {
            builder
                    .append(error.getMessage())
                    .append(",");
        }

        return builder.toString();
    }


    public DynFormDataDTO getFormData() {
        return formData;
    }


    public void setFormData(DynFormDataDTO data) {
        this.formData = data;
    }


    public List<FieldMessage> getFormFieldErrors() {
        return formFieldErrors;
    }


    public void setFormFieldErrors(List<FieldMessage> fieldErrors) {
        this.formFieldErrors = fieldErrors;
    }


}
