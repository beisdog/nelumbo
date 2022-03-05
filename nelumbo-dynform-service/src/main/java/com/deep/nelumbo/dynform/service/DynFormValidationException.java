package com.deep.nelumbo.dynform.service;

import java.util.List;
import com.merckgroup.util.dynform.dto.DynFormDataDTO;
import com.merckgroup.util.dynform.dto.FieldMessage;
import com.merckgroup.util.dynform.dto.Message;


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
        
        for (Message error: list) {
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
