package com.deep.nelumbo.dynform.events;

import com.deep.nelumbo.dynform.dto.DynFormConfigDTO;
import com.deep.nelumbo.dynform.dto.DynFormDataDTO;
import com.deep.nelumbo.dynform.dto.DynFormDataNodeDTO;
import com.deep.nelumbo.dynform.dto.DynFormFieldDTO;

/**
 * The format that is used as input in the valuesetProvider if you define a rest method there.
 *
 * @author X200531
 */
public class DynFormOnChangeRequest {

    private String sourceElementId;
    //Path to value in FormData
    private String dataPath;
    private String eventType = "Change";
    private DynFormDataDTO formData;
    private DynFormConfigDTO formConfig;

    public DynFormOnChangeRequest() {
    }

    public DynFormOnChangeRequest(
            String sourceElmentId,
            DynFormDataDTO formData,
            DynFormConfigDTO formConfig
    ) {
        super();
        this.sourceElementId = sourceElmentId;
        this.formData = formData;
        this.formConfig = formConfig;
    }

    public DynFormDataDTO getFormData() {
        return this.formData;
    }

    public void setFormData(DynFormDataDTO formData) {
        this.formData = formData;
    }

    public DynFormConfigDTO getFormConfig() {
        return this.formConfig;
    }

    public void setFormConfig(DynFormConfigDTO formConfig) {
        this.formConfig = formConfig;
    }

    public String getSourceElementId() {
        return this.sourceElementId;
    }

    public void setSourceElementId(String sourceElementId) {
        this.sourceElementId = sourceElementId;
    }

    public String getEventType() {
        return this.eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDataPath() {
        return this.dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public DynFormDataNodeDTO getSourceNode() {
        String path = this.getPathToSourceNode();
        if (path == null) {
            return null;
        }
        return this.formData.getNodeByPath(path);
    }

    public Integer getSourceNodeRowIndexOrNull() {
        String path = this.getPathToSourceNode();
        if (path == null) {
            return null;
        }
        int pos = path.lastIndexOf('/');
        String sIndex = path.substring(pos + 1);
        try {
            return Integer.parseInt(sIndex);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getPathToSourceNode() {
        String path = this.getPathToSourceField();
        if (path == null) {
            return null;
        }
        path = path.replace("/" + this.sourceElementId, "");
        return path;
    }

    public DynFormFieldDTO getSourceField() {
        String path = this.getPathToSourceField();
        if (path == null) {
            return null;
        }
        DynFormFieldDTO field = this.formData.getFieldByPath(path);
        return field;
    }

    public String getPathToSourceField() {
        String dataPath = this.getDataPath();
        if (dataPath != null) {
            dataPath = dataPath.replace("/data", "");
            dataPath = dataPath.replace("/childNodeList", "");
            dataPath = dataPath.replace("/childNode", "");
        }
        return dataPath;
    }
}
