package com.deep.nelumbo.dynform.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO that contains the UI configuration of a Form Field.
 * <p>
 * It is contained in {@link DynFormDataDTO}.
 *
 * @author X200531
 */
@Data
public class DynFormFieldConfigDTO {

    String state;
    String elementId;
    String parentElementId;
    String label;
    String type;
    Boolean enabled;
    Boolean editable;
    Boolean visible;
    Boolean required;
    Integer length;
    List<KeyValueDTO> values = null;
    String valueProvider;
    DynFormConfigNodeDTO children = new DynFormConfigNodeDTO();
}
