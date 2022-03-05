package com.deep.nelumbo.dynform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Individual ui settings that override the FieldConfig Settings. {@link DynFormFieldValueDTO}
 *
 * @author X200531
 */
@Data
@NoArgsConstructor
public class FieldUIState {

    private Boolean enabled;
    private Boolean editable;
    private Boolean visible;
    private Boolean required;
    private List<KeyValueDTO> values = null;

    public FieldUIState(
            Boolean enabled,
            Boolean editable,
            Boolean visible,
            Boolean required,
            List<KeyValueDTO> values
    ) {
        super();
        this.enabled = enabled;
        this.editable = editable;
        this.visible = visible;
        this.required = required;
        this.values = values;
    }

    public FieldUIState withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public FieldUIState withEditable(Boolean editable) {
        this.editable = editable;
        return this;
    }

    public FieldUIState withVisible(Boolean visible) {
        this.visible = visible;
        return this;
    }

    public FieldUIState withRequired(Boolean required) {
        this.required = required;
        return this;
    }

    public FieldUIState withValues(List<KeyValueDTO> values) {
        this.values = values;
        return this;
    }

    public FieldUIState addValue(String key, String value) {
        if (this.values == null) {
            this.values = new ArrayList<KeyValueDTO>();
        }
        this.values.add(new KeyValueDTO(key, value));
        return this;
    }

    public FieldUIState initFrom(DynFormFieldConfigDTO config) {
        this.enabled = config.getEnabled();
        this.editable = config.getEditable();
        this.visible = config.getEditable();
        this.required = config.getRequired();
        this.values = config.getValues();
        return this;
    }
}
