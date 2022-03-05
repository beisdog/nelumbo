package com.deep.nelumbo.dynform.dto;

import lombok.Data;

/**
 * DTO that contains the UI configuration of a form.
 *
 * @author X200531
 */
@Data
public class DynFormConfigDTO {

    private String id;
    private String task;
    private DynFormConfigNodeDTO elements = new DynFormConfigNodeDTO();

    public DynFormFieldConfigDTO getFormConfigByElementId(String elementId) {
        return getFormConfigByElementId(this.getElements(), elementId);
    }

    public static DynFormFieldConfigDTO getFormConfigByElementId(
            DynFormConfigNodeDTO elements,
            String elementId
    ) {
        if (elements.containsKey(elementId)) {
            return elements.getFieldConfig(elementId);
        }
        for (DynFormFieldConfigDTO child : elements.values()) {
            DynFormFieldConfigDTO result = getFormConfigByElementId(child.getChildren(), elementId);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Convenience method to access a nested form config.
     *
     * @param path e.g "MainGroup/SKU"
     * @return
     */
    public DynFormFieldConfigDTO getFormConfigByPath(String path) {
        String[] arPath = path.split("/");
        return getFormConfigByPath(this.getElements(), arPath);
    }

    public static DynFormFieldConfigDTO getFormConfigByPath(
            DynFormConfigNodeDTO elements,
            String... path
    ) {
        DynFormConfigNodeDTO map = elements;
        DynFormFieldConfigDTO last = null;
        for (String p : path) {
            if (map == null) {
                return null;
            }
            DynFormFieldConfigDTO current = map.getFieldConfig(p);
            if (current != null) {
                last = current;
                map = last.getChildren();
            } else {
                return null;
            }
        }
        if (last != null) {
            return last;
        }
        return null;
    }
}
