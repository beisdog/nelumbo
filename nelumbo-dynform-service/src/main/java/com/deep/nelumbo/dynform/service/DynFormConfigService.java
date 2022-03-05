package com.deep.nelumbo.dynform.service;

import com.deep.nelumbo.dynform.dto.DynFormConfigDTO;
import com.deep.nelumbo.dynform.dto.DynFormConfigNodeDTO;
import com.deep.nelumbo.dynform.dto.DynFormFieldConfigDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for reading the dynamic form configuration and assembling the DTOs.
 *
 * @author X200531
 * @see com.deep.nelumbo.dynform.dto.DynFormConfigDTO
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DynFormConfigService {

    private final static String DEFAULT = "DEFAULT";

    /**
     * Load the form configuration from the brm table and convert the table structure to a
     * {@link com.deep.nelumbo.dynform.dto.DynFormConfigDTO}.
     *
     * @return the dto
     */

    public DynFormConfigDTO getFormConfig(RuleLocationConfigDynForm parameterObject, String task) {
        final DynFormConfigDTO result = new DynFormConfigDTO();
        result.setId(parameterObject.getFormName());
        result.setTask(task);
        result.setFormConfigParameter(parameterObject);
        final DynFormConfigNodeDTO elements = new DynFormConfigNodeDTO();
        result.setElements(elements);

        // first step put all fields into a map for easy access
        final Map<String, DynFormFieldConfigDTO> fieldDtoMap = this.getFormConfigAsMap(
                parameterObject,
                task
        );
        // second step assign children to the parents
        for (final DynFormFieldConfigDTO field : fieldDtoMap.values()) {
            final String elementId = field.getElementId();
            final String parentId = field.getParentElementId();
            final DynFormFieldConfigDTO dto = fieldDtoMap.get(elementId);
            if (dto != null) {
                if (parentId != null && !parameterObject.getFormName().equals(parentId)) {
                    final DynFormFieldConfigDTO parentDto = fieldDtoMap.get(parentId);
                    if (parentDto != null) {
                        parentDto.getChildren().setFieldConfig(elementId, dto);
                    }
                } else {
                    // this is a toplevel element
                    elements.setFieldConfig(elementId, dto);
                }
            }
        }
        return result;
    }

    public Map<String, DynFormFieldConfigDTO> getFormConfigAsMap(
            RuleLocationConfigDynForm parameterObject,
            String task
    ) {

        Map<String, DynFormFieldConfigDTO> resultMap = new LinkedHashMap<String, DynFormFieldConfigDTO>();
        // First load the default values. They can be overridden then by the specific values.
        if (!DEFAULT.equalsIgnoreCase(task.trim())) {
            //resultMap = getFormConfigAsMap(parameterObject.getFormName(), DEFAULT);

            this.addFormConfigToMap(parameterObject, DEFAULT, resultMap);
        }
        return this.addFormConfigToMap(parameterObject, task, resultMap);
    }

    private Map<String, DynFormFieldConfigDTO> addFormConfigToMap(
            RuleLocationConfigDynForm parameterObject,
            String task,
            Map<String, DynFormFieldConfigDTO> resultMap
    ) {
        final List<DynFormToField> forms =
                this.rulesInvoker.getFormFieldsByFormName(parameterObject, task, new Integer(0));
        // first step put all fields into a map for easy access

        final List<DynFormToField.FormField> parentFields = new ArrayList<DynFormToField.FormField>();
        for (final DynFormToField form : forms) {
            for (final DynFormToField.FormField field : form.getFields()) {
                // if element starts with * it means that it shall inherit the fields from another task
                // e.g. *INITIAL means get the information from the task "INITIAL"
                if (field.getElementId().startsWith("*")) {
                    parentFields.add(field);
                } else {
                    final String elementId = field.getElementId();
                    final DynFormFieldConfigDTO dto = new DynFormFieldConfigDTO();
                    dto.setTask(form.getTask());
                    dto.setParentElementId(field.getParentElementId());
                    dto.setElementId(field.getElementId());
                    dto.setLabel(field.getLabel());
                    dto.setType(field.getType());
                    dto.setEditable(field.getEditable());
                    dto.setEnabled(field.getEnabled());
                    dto.setRequired(field.getRequired());
                    dto.setVisible(field.getVisible());
                    final String valueProvider = field.getValueProvider();
                    if (valueProvider != null) {
                        // RuleSetLocation rulesLocation = new RuleSetLocation(parameterObject.getBrmDcName(), parameterObject.getRulesetNameValueSet());
                        this.resolveValueProvider(parameterObject, dto, valueProvider);
                    }
                    dto.setValueProvider(field.getValueProvider());
                    resultMap.put(elementId, dto);
                }
            }
            // Get the parent fieldconfigs from the parent but only add if they do not exist yet
            for (DynFormToField.FormField f : parentFields) {
                // get the list children after the *, example *INITIAL-> INITIAL
                String parentTask = f.getElementId().substring(1);
                if (parentTask.equals(task)) {
                    throw new IllegalStateException(
                            "You have created an endless loop. The row: task=" + f.getFormToField()
                                    .getTask() + " position=" + f.getFormToField().getPosition()
                                    + " references the same parent task: " + task);
                }
                Map<String, DynFormFieldConfigDTO> parentTasks = new LinkedHashMap<String, DynFormFieldConfigDTO>();
                this.addFormConfigToMap(parameterObject, parentTask, parentTasks);
                for (DynFormFieldConfigDTO child : parentTasks.values()) {
                    // parent elements only override DEFAULT values
                    DynFormFieldConfigDTO existing = resultMap.get(child.getElementId());
                    if (existing == null || DEFAULT.equalsIgnoreCase(existing.getTask())) {
                        resultMap.put(child.getElementId(), child);
                    }
                }
            }
        }
        return resultMap;
    }

    /**
     * If a {@link DynFormFieldConfigDTO} has a valueProvider the values are resolved here and inserted
     * into the values map.
     *
     * @param dto
     * @param valueProvider
     */
    private void resolveValueProvider(
            RuleLocationConfigDynForm configParameter,
            DynFormFieldConfigDTO dto,
            String valueProvider
    ) {
        if (valueProvider.startsWith("KV:")) {
            final String kvString = valueProvider.substring("KV:".length());
            final String[] kvSegments = kvString.split(",");
            final List<KeyValueDTO> kvs = new ArrayList<KeyValueDTO>(kvSegments.length);
            for (final String kvSegment : kvSegments) {
                final String[] arSplit = kvSegment.split("=");
                final KeyValueDTO kvDto = new KeyValueDTO();
                kvDto.setKey(arSplit[0]);
                kvDto.setValue(arSplit[1]);
                kvs.add(kvDto);
            }
            dto.setValues(kvs);
        }
        if (valueProvider.startsWith("VS:")) {
            final String vsString = valueProvider.substring("VS:".length());
            final List<ValueSetToValues> vsList = this.vsRulesInvoker
                    .getValueset(
                            configParameter.getBrmDcName(),
                            configParameter.getRulesetNameValueSet(),
                            vsString,
                            0
                    );
            final List<KeyValueDTO> kvs = new ArrayList<KeyValueDTO>();
            for (final ValueSetToValues vs : vsList) {
                for (final Value v : vs.getValues()) {
                    final KeyValueDTO kvDto = new KeyValueDTO();
                    kvDto.setKey(v.getKey());
                    kvDto.setValue(v.getValue());
                    kvs.add(kvDto);
                }
            }
            dto.setValues(kvs);
        }
    }

    /**
     * Just for easy testing to get a dummy FormConfig without hitting the BRM.
     */
    @Override
    public DynFormConfigDTO getTestFormConfig() {
        DynFormConfigDTO result = new DynFormConfigDTO();
        final ObjectMapper mapper = new ObjectMapper();
        final InputStream in = this.getClass().getResourceAsStream("/test/form.config.json");
        try {
            result = mapper.readValue(in, DynFormConfigDTO.class);
        } catch (final IOException e) {
            throw new EJBException(e);
        }
        return result;
    }

    /**
     * Quick test
     *
     * @param args
     * @throws JsonProcessingException
     */
    public static void main(String[] args) throws JsonProcessingException {
        final DynFormConfigDTO formConfig = new DynFormConfigService().getTestFormConfig();
        System.out.println("FormConfig: " + formConfig);
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(formConfig);
        System.out.println(json);
    }
}
