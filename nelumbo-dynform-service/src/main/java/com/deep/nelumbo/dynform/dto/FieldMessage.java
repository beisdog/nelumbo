package com.deep.nelumbo.dynform.dto;

import com.deep.nelumbo.dynform.entity.DynFormValue;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hold validation message on a form field
 *
 * @author X200531
 */
@Data
@NoArgsConstructor
public class FieldMessage extends Message {

    String elementId;
    Integer index;

    public FieldMessage(String elementId, Integer index, String message) {
        super();
        this.elementId = elementId;
        this.index = index;
        this.message = message;
    }

    public FieldMessage(DynFormValue val, String message) {
        this(val.getElementId(), val.getIndex(), message);
    }
}
