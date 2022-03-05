package com.deep.nelumbo.dynform.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Belongs to {@link DynFormDataDTO}.
 *
 * @author X200531
 */
@Data
@NoArgsConstructor
public class DynFormFieldDTO {

    private String elementId;
    private Object value;
    private DynFormDataNodeDTO childNode;
    private List<DynFormDataNodeDTO> childNodeList = null;
    private List<FieldMessage> fieldMessages = null;
    /**
     * This can overwrite the {@link DynFormFieldConfigDTO} by Business Logic.
     * Initially the {@link DynFormFieldConfigDTO} settings are used and this object is null.
     * This is bound to the UI.
     */
    private FieldUIState uiState;

    public DynFormFieldDTO(String elementId) {
        super();
        this.elementId = elementId;
    }

    public DynFormFieldDTO(String elementId, Object value) {
        super();
        this.elementId = elementId;
        this.value = value;
    }

    public DynFormFieldDTO withValue(Object value) {
        this.value = value;
        return this;
    }

    public DynFormFieldDTO withChildNode(DynFormDataNodeDTO childMap) {
        this.childNode = childMap;
        return this;
    }

    public List<DynFormDataNodeDTO> getChildNodeList() {
        return this.childNodeList;
    }

    /**
     * Just use for loops
     *
     * @return
     */
    public List<DynFormDataNodeDTO> getChildNodeListOrEmpty(boolean initChildListIfNull) {
        if (this.childNodeList == null) {
            if (initChildListIfNull) {
                this.childNodeList = new ArrayList<DynFormDataNodeDTO>();
            } else {
                return new ArrayList<DynFormDataNodeDTO>();
            }
        }
        return this.childNodeList;
    }

    public DynFormFieldDTO addChildNodeListElement(DynFormDataNodeDTO node) {
        this.getChildNodeListOrEmpty(true).add(node);
        return this;
    }

    public void setChildNodeList(List<DynFormDataNodeDTO> childList) {
        this.childNodeList = childList;
    }

    public DynFormFieldDTO withChildNodeList(List<DynFormDataNodeDTO> childList) {
        this.childNodeList = childList;
        return this;
    }

    public List<FieldMessage> getFieldMessages() {
        return this.fieldMessages;
    }

    public void setFieldMessages(List<FieldMessage> fieldMessages) {
        this.fieldMessages = fieldMessages;
    }

    public DynFormFieldDTO withFieldMessages(List<FieldMessage> fieldMessages) {
        this.fieldMessages = fieldMessages;
        return this;
    }

    public DynFormFieldDTO addFieldMessage(FieldMessage fieldMessage) {
        if (this.fieldMessages == null) {
            this.fieldMessages = new ArrayList<FieldMessage>();
        }
        this.fieldMessages.add(fieldMessage);
        return this;
    }

    public String getValueState() {
        MessageType result = MessageType.None;
        if (this.fieldMessages != null) {
            for (Message msg : this.fieldMessages) {
                int ordinal = msg.type.ordinal();
                if (ordinal > result.ordinal()) {
                    result = msg.type;
                }
            }
        }
        return result.toString();
    }

    public void setValueState(String txt) {
        // do nothing, just needed for JSON
    }

    public String getValueStateText() {
        StringBuilder valueStateText = new StringBuilder();
        if (this.fieldMessages != null) {
            for (Message msg : this.fieldMessages) {
                valueStateText.append(msg.message).append(",");
            }
        }
        String result = valueStateText.toString();
        // remove last comma
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }

    public void setValueStateText(String txt) {
        // do nothing, just needed for JSON, but the getter is calculated according to the fieldmessages
    }

    public FieldUIState getUiState() {
        return this.uiState;
    }

    public void setUiState(FieldUIState state) {
        this.uiState = state;
    }

    public DynFormFieldDTO withUiState(FieldUIState state) {
        this.uiState = state;
        return this;
    }

    public DynFormFieldDTO withElementId(String elementId) {
        this.elementId = elementId;
        return this;
    }

    public static DynFormFieldDTO create(DynFormFieldConfigDTO fieldConfig) {
        DynFormFieldDTO valueDTO = new DynFormFieldDTO();
        valueDTO.setElementId(fieldConfig.getElementId());
        final FieldType type = FieldType.valueOf(fieldConfig.getType());
        valueDTO.setValue(type.defaultValue);
        return valueDTO;
    }

    @Override
    public String toString() {
        return "DynFormFieldDTO [elementId=" + this.elementId + ", value=" + this.value + ", childNode="
                + this.childNode + ", childNodeList=" + this.childNodeList + "]";
    }

    @JsonIgnore
    public String getValueAsString() {
        Object attrVal = this.getValue();
        if (attrVal == null) {
            return null;
        }
        if (attrVal instanceof String) {
            return (String) attrVal;
        }
        return String.valueOf(attrVal);
    }
}
