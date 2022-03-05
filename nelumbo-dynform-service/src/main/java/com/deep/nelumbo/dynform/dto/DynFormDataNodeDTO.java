package com.deep.nelumbo.dynform.dto;

import com.deep.nelumbo.dynform.util.Visitor;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.*;
import java.util.Map.Entry;

/**
 * A collection of {@link DynFormFieldValueLineItemDTO}s.
 * Wrapper around a map, so that an individual row can also have some ui state.
 *
 * @author X200531
 */
@Data
public class DynFormDataNodeDTO {
    private static final long serialVersionUID = 1157244745337191468L;

    private NodeUIState uiState;

    private Map<String, DynFormFieldDTO> map = new LinkedHashMap<String, DynFormFieldDTO>();

    public DynFormDataNodeDTO() {
        super();
    }

    public DynFormDataNodeDTO(Map<? extends String, ? extends DynFormFieldDTO> m) {
        this.map = new LinkedHashMap<String, DynFormFieldDTO>(m);
    }

    public DynFormDataNodeDTO(DynFormFieldDTO... fields) {
        for (DynFormFieldDTO field : fields) {
            this.setField(field);
        }
    }

    public DynFormDataNodeDTO(DynFormConfigNodeDTO configNode) {
        this.initFromConfigNode(configNode);
    }

    public void setFieldValue(String key, Object value) {
        DynFormFieldDTO fieldDto = this.map.get(key);
        if (fieldDto == null) {
            fieldDto = new DynFormFieldDTO(key, value);
            this.map.put(key, fieldDto);
        } else {
            fieldDto.setValue(value);
        }
    }

    public DynFormDataNodeDTO withFieldValue(String key, Object value) {
        this.setFieldValue(key, value);
        return this;
    }

    public void setFieldUiState(String key, FieldUIState uiState) {
        DynFormFieldDTO valDto = this.map.get(key);
        if (valDto == null) {
            valDto = new DynFormFieldDTO(key, null);
            this.map.put(key, valDto);
        }
        valDto.setUiState(uiState);
    }

    public DynFormDataNodeDTO withFieldUiState(String key, FieldUIState uiState) {
        this.setFieldUiState(key, uiState);
        return this;
    }

    @JsonAnySetter
    public void setField(String key, DynFormFieldDTO field) {
        this.map.put(key, field);
    }

    public void setField(DynFormFieldDTO field) {
        this.map.put(field.getElementId(), field);
    }

    public DynFormDataNodeDTO withField(String key, DynFormFieldDTO field) {
        this.map.put(key, field);
        return this;
    }

    public DynFormDataNodeDTO withField(DynFormFieldDTO field) {
        this.map.put(field.getElementId(), field);
        return this;
    }

    @JsonAnyGetter
    public Map<String, DynFormFieldDTO> getFieldValueMap() {
        return this.map;
    }

    public DynFormFieldDTO getField(String key) {
        return this.map.get(key);
    }

    public String getFieldValueAsString(String key) {
        DynFormFieldDTO field = this.map.get(key);
        if (field == null) {
            return null;
        }
        return field.getValueAsString();
    }

    public Boolean getFieldValueAsBoolean(String key) {
        String attrVal = this.getFieldValueAsString(key);
        if (attrVal == null) {
            return null;
        }
        return Boolean.parseBoolean(attrVal);
    }

    public Integer getFieldValueAsInteger(String key) {
        String attrVal = this.getFieldValueAsString(key);
        if (attrVal == null) {
            return null;
        }
        return Integer.parseInt(attrVal);
    }

    public NodeUIState getUiState() {
        return this.uiState;
    }

    public void setUiState(NodeUIState uiState) {
        this.uiState = uiState;
    }

    public DynFormDataNodeDTO withUiState(NodeUIState uiState) {
        this.uiState = uiState;
        return this;
    }

    public DynFormDataNodeDTO withRemoveEnabled(boolean removeEnabled) {
        this.uiState = new NodeUIState(removeEnabled);
        return this;
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    public void clear() {
        this.map.clear();
    }

    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    public Set<Entry<String, DynFormFieldDTO>> entrySet() {
        return this.map.entrySet();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public void putAll(Map<? extends String, ? extends DynFormFieldDTO> m) {
        this.map.putAll(m);
    }

    public DynFormFieldDTO remove(Object key) {
        return this.map.remove(key);
    }

    public int size() {
        return this.map.size();
    }

    public Collection<DynFormFieldDTO> values() {
        return this.map.values();
    }

    public static DynFormDataNodeDTO createFromConfigNode(DynFormConfigNodeDTO configNode) {
        DynFormDataNodeDTO dataNode = new DynFormDataNodeDTO();
        initFromConfigNode(dataNode, configNode);
        return dataNode;
    }

    public DynFormDataNodeDTO initFromConfigNode(DynFormConfigNodeDTO configNode) {
        initFromConfigNode(this, configNode);
        return this;
    }

    public static void initFromConfigNode(
            DynFormDataNodeDTO dataNode,
            DynFormConfigNodeDTO configNode
    ) {
        for (final String key : configNode.keySet()) {
            final DynFormFieldConfigDTO fieldConfig = configNode.getFieldConfig(key);

            final DynFormFieldDTO valueDTO = DynFormFieldDTO.create(fieldConfig);
            final FieldType type = FieldType.valueOf(fieldConfig.getType());

            dataNode.setField(key, valueDTO);

            if (type.hasChildren && !type.hasIndexedChildren) {
                // has children but not a list
                final DynFormConfigNodeDTO childConfigNode = fieldConfig.getChildren();
                final DynFormDataNodeDTO childNode = new DynFormDataNodeDTO();
                initFromConfigNode(childNode, childConfigNode);
                valueDTO.setChildNode(childNode);
            } else if (type.hasChildren && type.hasIndexedChildren) {
                final DynFormConfigNodeDTO childConfigNode = fieldConfig.getChildren();
                final DynFormDataNodeDTO childNode = new DynFormDataNodeDTO();
                initFromConfigNode(childNode, childConfigNode);
                final List<DynFormDataNodeDTO> childrenList = new ArrayList<DynFormDataNodeDTO>();
                // just create a single row of empty data
                //childrenList.add(subData);
                valueDTO.setChildNodeList(childrenList);
            }
        }
    }

    @Override
    public String toString() {
        return "DynFormDataNodeDTO " + this.map;
    }

    public void traverseFieldsRecursively(Visitor<DynFormFieldDTO> visitor) {
        for (DynFormFieldDTO field : this.getFieldValueMap().values()) {
            if (!visitor.visit(field)) {
                return;
            }
            if (field.getChildNode() != null) {
                field.getChildNode().traverseFieldsRecursively(visitor);
            }
            if (field.getChildNodeList() != null) {
                for (DynFormDataNodeDTO node : field.getChildNodeList()) {
                    node.traverseFieldsRecursively(visitor);
                }
            }
        }
    }

    @JsonIgnore
    public List<DynFormFieldDTO> getAllFieldsIncludingChildrenFields() {
        final List<DynFormFieldDTO> fields = new ArrayList<DynFormFieldDTO>();
        Visitor<DynFormFieldDTO> visitor = new Visitor<DynFormFieldDTO>() {

            @Override
            public boolean visit(DynFormFieldDTO t) {
                fields.add(t);
                return true;
            }
        };
        this.traverseFieldsRecursively(visitor);
        return fields;
    }
}
