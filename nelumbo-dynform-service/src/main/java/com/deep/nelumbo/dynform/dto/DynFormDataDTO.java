package com.deep.nelumbo.dynform.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Data object containing the field values of a form in a hierarchical form.
 * Example:
 * <pre>
 * {
 * "id": "LAC_570",
 * "formName": "LAC",
 * "task": "REQUESTER",
 * "data": {
 * "MainGroup": {
 * "childNode":
 * {
 * "CCNo": { "value": null},
 * "SKU": { "value": ""},
 * "ProductName": { "value": null},
 * "ImplDate": { "value": null},
 * "Country": { "value": null},
 * "Customer": { "value": null},
 * "ProductGroup": { "value": null}
 * }
 * },
 * "BOMList": {
 * childNodeList: [
 * {
 * "PackMat": { "value": "1"},
 * "PackSize": { "value": null},
 * "Purpose": { "value": null},
 * "PackMatNew": { "value": null},
 * "PackMatOld": { "value": null}
 * },
 * {
 * "PackMat": { "value": "2"},
 * "PackSize": { "value": null},
 * "Purpose": { "value": null},
 * "PackMatNew": { "value": null},
 * "PackMatOld": { "value": null}
 * }
 * ]
 * }
 * }
 * }
 * }
 * </pre>
 *
 * @author X200531
 */
@Data
public class DynFormDataDTO {

    private String id;
    private String task;

    //@JsonSerialize(as = LinkedHashMap.class)
    private DynFormDataNodeDTO data = new DynFormDataNodeDTO();

    /**
     * Used like a session store so the application can store data
     * on the client that it can reuse in its business logic.
     * This data is not saved to the database! As value you can only
     * use primitimve types, lists or other maps because it is serialized to json
     * all other type information is lost.
     */
    private Map<String, Object> hiddenData = new HashMap<>();

    /**
     * Convenience method to access a nested property.
     *
     * @param path e.g "MainGroup/SKU"
     * @return
     */
    public String getFieldValueByPath(String path) {
        String[] arPath = path.split("/");
        return getFieldValueByPath(this.getData(), arPath);
    }

    /**
     * Convenience method to access a nested property.
     *
     * @param path e.g "MainGroup/SKU"
     * @return
     */
    public DynFormFieldDTO getFieldByPath(String path) {
        if (path == null) {
            return null;
        }
        String cleanedPath = path;
        if (cleanedPath.startsWith("/")) {
            cleanedPath = cleanedPath.substring(1);
        }
        String[] arPath = cleanedPath.split("/");
        return getFieldByPath(this.getData(), arPath);
    }

    public DynFormDataNodeDTO getNodeByPath(String path) {
        String cleanedPath = path;
        if (cleanedPath.startsWith("/")) {
            cleanedPath = cleanedPath.substring(1);
        }
        String[] arPath = cleanedPath.split("/");
        return getNodeByPath(this.getData(), arPath);
    }

    public boolean setFieldValueByPath(String path, Object value) {
        String[] arPath = path.split("/");
        DynFormFieldDTO property = getFieldByPath(this.getData(), arPath);
        if (property != null) {
            property.setValue(value);
            return true;
        }
        return false;
    }

    public static String getFieldValueByPath(DynFormDataNodeDTO dynFormData, String... path) {
        DynFormFieldDTO last = getFieldByPath(dynFormData, path);
        if (last != null) {
            if (last.getValue() == null) {
                return null;
            }
            return String.valueOf(last.getValue());
        }
        return null;
    }

    public static DynFormFieldDTO getFieldByPath(DynFormDataNodeDTO dynFormData, String... path) {
        DynFormDataNodeDTO node = dynFormData;
        DynFormFieldDTO last = null;
        for (String p : path) {
            Integer index = parseIntOrNull(p);
            if (index != null) {
                if (last == null || last.getChildNodeList() == null) {
                    return null;
                }
                node = null;
                if (last.getChildNodeList().size() > index) {
                    node = last.getChildNodeList().get(index);
                } else {
                    return null;
                }
            } else {
                if (node == null) {
                    return null;
                }
                DynFormFieldDTO current = node.getField(p);
                if (current != null) {
                    last = current;
                    node = last.getChildNode();
                } else {
                    return null;
                    //throw new IllegalArgumentException("Value '" + p + "' not found in Dynform! path:" + p);
                }
            }
        }
        if (last != null) {
            return last;
        }
        return null;
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null) {
            return null;
        }
        char[] chars = s.toCharArray();
        int result = 0;
        int multiply = 1;
        int zeroAsI = (int) '0';
        for (int pos = chars.length - 1; pos >= 0; pos--) {
            char c = chars[pos];
            int digit = (int) c - zeroAsI;
            if (digit > 9 || digit < 0) {
                //this is not a digit anymore, parsing error
                return null;
            }
            result = result + (digit * multiply);
            multiply = multiply * 10;
        }
        return result;
    }

    public static DynFormDataNodeDTO getNodeByPath(DynFormDataNodeDTO dynFormData, String... path) {
        DynFormDataNodeDTO node = dynFormData;
        DynFormFieldDTO last = null;
        for (String p : path) {
            Integer index = parseIntOrNull(p);
            if (index != null) {
                if (last == null || last.getChildNodeList() == null) {
                    return null;
                }
                node = null;
                if (last.getChildNodeList().size() > index) {
                    node = last.getChildNodeList().get(index);
                } else {
                    return null;
                }
            } else {
                if (node == null) {
                    return null;
                }
                DynFormFieldDTO current = node.getField(p);
                if (current != null) {
                    last = current;
                    node = last.getChildNode();
                } else {
                    return null;
                    //throw new IllegalArgumentException("Value '" + p + "' not found in Dynform! path:" + p);
                }
            }
        }
        if (last != null) {
            return node;
        }
        return null;
    }

    public Map<String, Object> getHiddenData() {
        return this.hiddenData;
    }

    public void setHiddenData(Map<String, Object> hiddenData) {
        this.hiddenData = hiddenData;
    }
}
