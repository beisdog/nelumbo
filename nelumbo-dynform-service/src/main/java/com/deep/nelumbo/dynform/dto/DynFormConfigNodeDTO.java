package com.deep.nelumbo.dynform.dto;

import com.deep.nelumbo.dynform.util.Visitor;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.util.Map.Entry;

public class DynFormConfigNodeDTO {

    /**
     * A node is a collection {@link DynFormFieldConfigDTO}.
     */
    private Map<String, DynFormFieldConfigDTO> map = new LinkedHashMap<String, DynFormFieldConfigDTO>();

    public DynFormConfigNodeDTO() {
        super();
    }

    public DynFormConfigNodeDTO(Map<? extends String, ? extends DynFormFieldConfigDTO> m) {
        this.map = new LinkedHashMap<String, DynFormFieldConfigDTO>(m);
    }

    @JsonAnySetter
    public void setFieldConfig(String key, DynFormFieldConfigDTO val) {
        this.map.put(key, val);
    }

    public DynFormFieldConfigDTO getFieldConfig(String key) {
        return this.map.get(key);
    }

    @JsonAnyGetter
    public Map<String, DynFormFieldConfigDTO> getFieldConfigMap() {
        return this.map;
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set<Entry<String, DynFormFieldConfigDTO>> entrySet() {
        return map.entrySet();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public DynFormFieldConfigDTO remove(Object key) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Collection<DynFormFieldConfigDTO> values() {
        return map.values();
    }

    public void traverseFieldsRecursively(Visitor<DynFormFieldConfigDTO> visitor) {
        for (DynFormFieldConfigDTO field : this.getFieldConfigMap().values()) {
            if (!visitor.visit(field)) {
                return;
            }
            if (field.getChildren() != null) {
                field.getChildren().traverseFieldsRecursively(visitor);
            }
        }
    }

    @JsonIgnore
    public List<DynFormFieldConfigDTO> getAllFieldsIncludingChildrenFields() {
        final List<DynFormFieldConfigDTO> fields = new ArrayList<DynFormFieldConfigDTO>();
        Visitor<DynFormFieldConfigDTO> visitor = new Visitor<DynFormFieldConfigDTO>() {

            @Override
            public boolean visit(DynFormFieldConfigDTO t) {
                fields.add(t);
                return true;
            }
        };
        traverseFieldsRecursively(visitor);
        return fields;
    }
}
