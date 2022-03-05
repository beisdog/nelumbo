package com.deep.nelumbo.dynform.events;

import com.deep.nelumbo.dynform.dto.DynFormConfigDTO;
import com.deep.nelumbo.dynform.dto.DynFormDataDTO;
import com.deep.nelumbo.dynform.dto.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The response structure if a OnEvent rest call is returned.
 *
 * @author X200531
 */
public class DynFormOnChangeResponse extends DynFormOnChangeRequest {

    Map<String, String> overwrittenValues;
    List<Message> messages = new ArrayList<Message>();

    public DynFormOnChangeResponse() {
    }

    public DynFormOnChangeResponse(
            String sourceElmentId,
            DynFormDataDTO formData,
            DynFormConfigDTO formConfig,
            Map<String, String> overwrittenValues
    ) {
        super(sourceElmentId, formData, formConfig);
        this.overwrittenValues = overwrittenValues;
    }

    public Map<String, String> getOverwrittenValues() {
        return this.overwrittenValues;
    }

    public void setOverwrittenValues(Map<String, String> overwrittenValues) {
        this.overwrittenValues = overwrittenValues;
    }

    public List<Message> getMessages() {
        return this.messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
