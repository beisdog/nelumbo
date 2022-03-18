package com.deep.nelumbo.dynform.service;

import com.deep.nelumbo.dynform.dto.DynFormConfigDTO;
import com.deep.nelumbo.dynform.dto.DynFormConfigNodeDTO;
import com.deep.nelumbo.dynform.dto.DynFormFieldConfigDTO;
import com.deep.nelumbo.dynform.dto.KeyValueDTO;
import com.deep.nelumbo.dynform.entity.DynFormConfigEntity;
import com.deep.nelumbo.dynform.entity.DynFormFieldConfigEntity;
import com.deep.nelumbo.dynform.entity.KeyValueEntity;
import com.deep.nelumbo.dynform.repo.DynFormConfigRepo;
import com.deep.nelumbo.dynform.repo.DynFormFieldConfigRepo;
import com.deep.nelumbo.dynform.repo.KeyValueRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Responsible for reading the dynamic form configuration and assembling the DTOs.
 *
 * @author X200531
 * @see com.deep.nelumbo.dynform.dto.DynFormConfigDTO
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class DynFormConfigService {

    private final static String DEFAULT = "DEFAULT";

    private final DynFormConfigRepo formConfigRepo;
    private final DynFormFieldConfigRepo fieldConfigRepo;
    private final KeyValueRepo keyValueRepo;

    /**
     * Load the form configuration from the brm table and convert the table structure to a
     * {@link com.deep.nelumbo.dynform.dto.DynFormConfigDTO}.
     *
     * @return the dto
     */

    public DynFormConfigDTO getFormConfig(String formConfigId, String state) {
        final DynFormConfigDTO result = new DynFormConfigDTO();
        result.setId(formConfigId);
        result.setState(state);
        final DynFormConfigNodeDTO elements = new DynFormConfigNodeDTO();
        result.setElements(elements);

        // first step put all fields into a map for easy access
        final Map<String, DynFormFieldConfigDTO> fieldDtoMap = this.getFormConfigAsMap(
                formConfigId, state
        );
        // second step assign children to the parents
        for (final DynFormFieldConfigDTO field : fieldDtoMap.values()) {
            final String elementId = field.getElementId();
            final String parentId = field.getParentElementId();
            final DynFormFieldConfigDTO dto = fieldDtoMap.get(elementId);
            if (dto != null) {
                if (parentId != null && !formConfigId.equals(parentId)) {
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
            String formConfigId,
            String state
    ) {

        Map<String, DynFormFieldConfigDTO> resultMap = new LinkedHashMap<String, DynFormFieldConfigDTO>();
        // First load the default values. They can be overridden then by the specific values.
        if (!DEFAULT.equalsIgnoreCase(state)) {
            //resultMap = getFormConfigAsMap(parameterObject.getFormName(), DEFAULT);

            this.addFormConfigToMap(formConfigId, DEFAULT, resultMap);
        }
        return this.addFormConfigToMap(formConfigId, state, resultMap);
    }

    private Map<String, DynFormFieldConfigDTO> addFormConfigToMap(
            String formConfigId,
            String state,
            Map<String, DynFormFieldConfigDTO> resultMap
    ) {
        final List<DynFormFieldConfigEntity> fields = this.fieldConfigRepo.findByFormConfigIdAndState(formConfigId, state);
        // first step put all fields into a map for easy access

        final List<DynFormFieldConfigEntity> parentFields = new ArrayList<>();
        for (final DynFormFieldConfigEntity field : fields) {
            // if element starts with * it means that it shall inherit the fields from another task
            // e.g. *INITIAL means get the information from the task "INITIAL"
            if (field.getElementId().startsWith("*")) {
                parentFields.add(field);
            } else {
                final String elementId = field.getElementId();
                final DynFormFieldConfigDTO dto = new DynFormFieldConfigDTO();
                dto.setState(field.getState());
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
                        this.resolveValueProvider(dto, valueProvider);
                    }
                    dto.setValueProvider(field.getValueProvider());
                    resultMap.put(elementId, dto);
                }
            // Get the parent fieldconfigs from the parent but only add if they do not exist yet
            for (DynFormFieldConfigEntity f : parentFields) {
                // get the list children after the *, example *INITIAL-> INITIAL
                String parentState = f.getElementId().substring(1);
                if (parentState.equals(state)) {
                    throw new IllegalStateException(
                            "You have created an endless loop. The row: state=" + f
                                    .getState() + " position=" + f.getPosition()
                                    + " references the same parent task: " + state);
                }
                Map<String, DynFormFieldConfigDTO> parentTasks = new LinkedHashMap<String, DynFormFieldConfigDTO>();
                this.addFormConfigToMap(formConfigId, parentState, parentTasks);
                for (DynFormFieldConfigDTO child : parentTasks.values()) {
                    // parent elements only override DEFAULT values
                    DynFormFieldConfigDTO existing = resultMap.get(child.getElementId());
                    if (existing == null || DEFAULT.equalsIgnoreCase(existing.getState())) {
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
            final List<KeyValueEntity> vsList = this.keyValueRepo.findByType(vsString);
            final List<KeyValueDTO> kvs = new ArrayList<KeyValueDTO>();
            for (final KeyValueEntity v : vsList) {
                final KeyValueDTO kvDto = new KeyValueDTO();
                kvDto.setKey(v.getKey());
                kvDto.setValue(v.getValue());
                kvs.add(kvDto);
            }
            dto.setValues(kvs);
        }
    }

    /**
     * Just for easy testing to get a dummy FormConfig without hitting the BRM.
     */
    public DynFormConfigDTO getTestFormConfig() {
        DynFormConfigDTO result = new DynFormConfigDTO();
        final ObjectMapper mapper = new ObjectMapper();
        final InputStream in = this.getClass().getResourceAsStream("/test/form.config.json");
        try {
            result = mapper.readValue(in, DynFormConfigDTO.class);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    public DynFormConfigEntity save(DynFormConfigDTO formConfig) {

        DynFormConfigEntity formConfigEntity = new DynFormConfigEntity();
        formConfigEntity.setId(formConfig.getId());
        String state = formConfig.getState();
        var parentNode = formConfig.getElements();
        createFieldConfigEntitiesFromNode(formConfigEntity, state, Optional.empty(), parentNode);
        return this.formConfigRepo.save(formConfigEntity);
    }

    private void createFieldConfigEntitiesFromNode(DynFormConfigEntity formConfigEntity, String state, Optional<String> parentElementId, DynFormConfigNodeDTO node) {
        var fieldConfigMap = node.getFieldConfigMap();
        int position = 0;
        for (var entry : fieldConfigMap.entrySet()) {
            position++;
            String elementId = entry.getKey();
            var fieldConfig = entry.getValue();
            createFieldConfigEntity(formConfigEntity, state, position, entry.getKey(), parentElementId, fieldConfig);
            createFieldConfigEntitiesFromNode(formConfigEntity, state, Optional.of(elementId), fieldConfig.getChildren());
        }
    }

    private void createFieldConfigEntity(DynFormConfigEntity formConfigEntity, String state, Integer position, String elementId, Optional<String> parentElementId, DynFormFieldConfigDTO src) {
        var target = new DynFormFieldConfigEntity();
        target.setFormConfigId(formConfigEntity.getId());
        target.setParentElementId(parentElementId.orElse(null));
        target.setElementId(elementId);
        target.setEnabled(src.getEnabled());
        target.setEditable(src.getEditable());
        target.setLabel(src.getLabel());
        target.setState(state);
        target.setRequired(src.getRequired());
        target.setType(src.getType());
        target.setLength(src.getLength());
        target.setPosition(position);
        target.setValueProvider(src.getValueProvider());
        target.setVisible(src.getVisible());
        formConfigEntity.getFieldConfigs().add(target);
    }

}
