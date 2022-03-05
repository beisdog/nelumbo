package com.deep.nelumbo.dynform.dto;

import lombok.Data;

/**
 * Data Transfer object that contains the field values of a form instance.
 * It is flat and only used by the {@link com.deep.nelumbo.dynform.service.DynFormDataService#searchForValues(String, DynFormDataDTO)}.
 *
 * @author X200531
 */
@Data
public class DynFormFieldValueLineItemDTO {

    private String id;
    private String formInstanceId;
    private String formName;
    private Integer index;
    private String parentElementId;
    private String elementId;
    private String stringValue;
}
