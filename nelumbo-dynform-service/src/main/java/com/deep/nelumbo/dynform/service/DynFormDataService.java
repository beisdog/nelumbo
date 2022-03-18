package com.deep.nelumbo.dynform.service;

import com.deep.nelumbo.dynform.dto.DynFormConfigDTO;
import com.deep.nelumbo.dynform.dto.DynFormConfigNodeDTO;
import com.deep.nelumbo.dynform.dto.DynFormDataDTO;
import com.deep.nelumbo.dynform.dto.DynFormDataNodeDTO;
import com.deep.nelumbo.dynform.dto.DynFormFieldConfigDTO;
import com.deep.nelumbo.dynform.dto.DynFormFieldDTO;
import com.deep.nelumbo.dynform.dto.FieldMessage;
import com.deep.nelumbo.dynform.dto.FieldType;
import com.deep.nelumbo.dynform.entity.DynFormValueEntity;
import com.deep.nelumbo.dynform.repo.DynFormValueRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Responsible for reading and writing the form data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynFormDataService {

    private static final String UTIL_DYNFRM_VALS_SEQ = "UTIL_DYNFRM_VALS_SEQ";

    private final DynFormConfigService formConfigService;
    private final DynFormValueRepo dynFormValueRepo;
    private final DynFormValueRepo repo;

    public DynFormDataDTO getTestFormData() {
        DynFormDataDTO result = new DynFormDataDTO();
        final ObjectMapper mapper = new ObjectMapper();
        final InputStream in = this.getClass().getResourceAsStream("/test/form.data.json");
        try {
            result = mapper.readValue(in, DynFormDataDTO.class);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    public DynFormDataDTO getFormData(String id, String formConfigId, String state) {
        DynFormConfigDTO formConfig = this.formConfigService.getFormConfig(formConfigId, state);
        return this.getFormData(id, formConfig);
    }

    public DynFormDataDTO getFormData(String id, DynFormConfigDTO formConfig) {
        final DynFormDataDTO result = new DynFormDataDTO();

        final List<DynFormValueEntity> topLevel = this.dynFormValueRepo.findByIdAndFormConfigIdAndParentIsNull(id, formConfig.getId());
        result.setFormConfigId(formConfig.getId());
        result.setId(id);
        result.setState(formConfig.getState());

        final DynFormDataNodeDTO data = this.createNodeFromDynFormValues(result, formConfig.getElements(), topLevel);

        result.setData(data);
        // TODO check if the other way works
        // for (final DynFormValue val : topLevel) {
        // fillDataNodeFromDynFormValue(formConfig, data, val);
        // }
        return result;
    }

    private DynFormDataNodeDTO createNodeFromDynFormValues(DynFormDataDTO form, DynFormConfigNodeDTO configNode, Collection<DynFormValueEntity> topLevel) {
        //First create node, with all empty values
        DynFormDataNodeDTO dataNode = DynFormDataNodeDTO.createFromConfigNode(configNode);
        // fill the values
        for (final DynFormValueEntity val : topLevel) {
            DynFormFieldConfigDTO fieldConfig = configNode.getFieldConfig(val.getElementId());
            this.setDynFormValue(form, dataNode, val, fieldConfig);
        }

        return dataNode;
    }

    private void setDynFormValue(DynFormDataDTO form, DynFormDataNodeDTO dataNode, DynFormValueEntity val, DynFormFieldConfigDTO fieldConfig) {
        final String key = val.getElementId();
        final DynFormFieldDTO valueDTO = new DynFormFieldDTO();
        valueDTO.setElementId(key);
        dataNode.setField(key, valueDTO);
        if (fieldConfig == null) {
            log.warn("FieldValue:" + val.getElementId() + " has no corresponding FieldConfiguration. Form " + form.getId() + "/" + form.getState());
        }
        if (fieldConfig != null && fieldConfig.getChildren() != null && FieldType.valueOf(fieldConfig.getType()).hasChildren) {
            FieldType fieldType = FieldType.valueOf(fieldConfig.getType());
            boolean hasIndex = fieldType == null ? false : fieldType.hasIndexedChildren;
            DynFormConfigNodeDTO configNode = fieldConfig.getChildren();
            if (hasIndex) {
                DynFormDataNodeDTO childNode = null;
                final Map<Integer, DynFormDataNodeDTO> childNodeMap = new TreeMap<Integer, DynFormDataNodeDTO>();
                for (final DynFormValueEntity childVal : val.getChildren()) {
                    final Integer index = childVal.getIndex();
                    childNode = childNodeMap.get(index);
                    if (childNode == null) {
                        childNode = DynFormDataNodeDTO.createFromConfigNode(configNode);
                        childNodeMap.put(index, childNode);
                    }
                    DynFormFieldConfigDTO childConfig = configNode.getFieldConfig(childVal.getElementId());
                    this.setDynFormValue(form, childNode, childVal, childConfig);
                }
                List<DynFormDataNodeDTO> childList = new ArrayList<DynFormDataNodeDTO>(childNodeMap.values());
                valueDTO.setChildNodeList(childList);
            } else {
                final DynFormDataNodeDTO childNode = DynFormDataNodeDTO.createFromConfigNode(configNode);
                for (final DynFormValueEntity childVal : val.getChildren()) {
                    DynFormFieldConfigDTO childConfig = configNode.getFieldConfig(childVal.getElementId());
                    this.setDynFormValue(form, childNode, childVal, childConfig);
                }
                valueDTO.setChildNode(childNode);
            }
        } else {
            valueDTO.setValue(val.getStringValue());
        }
    }


    /**
     * Create a new form and store it with empty values in the db.
     *
     * @param id
     * @param formConfigId
     * @param state
     * @return the created form
     */
    public DynFormDataDTO createNewFormAndSave(String id, String formConfigId, String state) {
        final DynFormConfigDTO formConfig = this.formConfigService.getFormConfig(formConfigId, state);
        return this.createNewFormAndSave(id, formConfig);
    }

    public DynFormDataDTO createNewFormAndSave(String formInstanceId, final DynFormConfigDTO formConfig) {
        final DynFormDataDTO form = new DynFormDataDTO();
        form.setId(formInstanceId);
        form.setFormConfigId(formConfig.getId());
        form.setState(formConfig.getState());
        final DynFormDataNodeDTO data = new DynFormDataNodeDTO();
        form.setData(data);
        // recursively loop through the config and create empty values for each
        final DynFormConfigNodeDTO elements = formConfig.getElements();
        DynFormDataNodeDTO.initFromConfigNode(data, elements);
        form.setData(data);
        this.saveFormData(form, true);
        return form;
    }

    /**
     * Create a new Form but do not store it in the database
     */
    public DynFormDataDTO newForm(String id, String formConfigId, String state) {
        final DynFormConfigDTO formConfig = this.formConfigService.getFormConfig(formConfigId, state);
        return this.newForm(id, formConfig);
    }

    public DynFormDataDTO newForm(String id, final DynFormConfigDTO formConfig) {
        final DynFormDataDTO form = new DynFormDataDTO();
        form.setId(id);
        form.setFormConfigId(formConfig.getId());
        form.setState(formConfig.getState());
        final DynFormDataNodeDTO data = new DynFormDataNodeDTO();
        form.setData(data);

        final DynFormConfigNodeDTO elements = formConfig.getElements();
        DynFormDataNodeDTO.initFromConfigNode(data, elements);
        form.setData(data);
        return form;
    }

    public DynFormDataNodeDTO newFormDataNode(DynFormConfigNodeDTO config) {
        DynFormDataNodeDTO result = new DynFormDataNodeDTO();
        DynFormDataNodeDTO.initFromConfigNode(result, config);
        return result;
    }

    public DynFormDataDTO saveFormData(DynFormDataDTO dto, boolean validate) {
        // delete former values and recreate them
        if (dto.getId() != null) {
            final List<DynFormValueEntity> list = this.dynFormValueRepo.findByFormInstanceId(dto.getId());
            for (final DynFormValueEntity dynFormValue : list) {
                this.dynFormValueRepo.delete(dynFormValue);
            }
        }
        // recreate the values
        final DynFormDataNodeDTO data = dto.getData();
        final List<DynFormValueEntity> vals = new ArrayList<DynFormValueEntity>();
        this.createVals(vals, dto, null, data);
        if (validate) {
            List<FieldMessage> errors = this.validateFormData(dto);
            if (errors.size() > 0) {
                throw new DynFormValidationException(dto, errors);
            }
        } else {
            // clear fieldmessages
            this.clearFieldMessages(dto);
        }
        for (final DynFormValueEntity val : vals) {
            this.dynFormValueRepo.save(val);
        }
        return dto;
    }

    private void clearFieldMessages(DynFormDataDTO dto) {
        for (String key : dto.getData().keySet()) {
            DynFormFieldDTO value = dto.getData().getField(key);
            this.clearFieldMessages(value);
        }
    }

    private void clearFieldMessages(DynFormFieldDTO value) {
        value.setFieldMessages(new ArrayList<FieldMessage>());
        if (value.getChildNode() != null) {
            for (String key : value.getChildNode().keySet()) {
                DynFormFieldDTO child = value.getChildNode().getField(key);
                this.clearFieldMessages(child);
            }
        }
        if (value.getChildNodeList() != null) {
            for (DynFormDataNodeDTO childNode : value.getChildNodeList()) {
                for (String key : childNode.keySet()) {
                    DynFormFieldDTO child = childNode.getField(key);
                    this.clearFieldMessages(child);
                }
            }
        }
    }

    public List<FieldMessage> validateFormData(DynFormDataDTO dto) {
        final DynFormDataNodeDTO data = dto.getData();
        DynFormConfigDTO config = this.formConfigService.getFormConfig(dto.getFormConfigId(), dto.getState());
        List<FieldMessage> allFieldMessages = new ArrayList<FieldMessage>();
        for (String key : config.getElements().keySet()) {
            DynFormFieldDTO value = data.getField(key);
            if (value != null) {
                allFieldMessages.addAll(this.validateField(config.getElements().getFieldConfig(key), value, 0));
            }
        }
        return allFieldMessages;
    }

    private List<FieldMessage> validateField(DynFormFieldConfigDTO fieldConfig, DynFormFieldDTO value, int index) {
        List<FieldMessage> allFieldMessages = new ArrayList<FieldMessage>();
        List<FieldMessage> fieldMessages = new ArrayList<FieldMessage>();

        if (fieldConfig == null) {
            return allFieldMessages;
        }
        FieldType type = FieldType.valueOf(fieldConfig.getType());

        if (fieldConfig.getRequired()) {
            String sValue = value.getValue() == null ? sValue = "" : String.valueOf(value.getValue());
            if (sValue.trim().length() == 0) {
                FieldMessage msg = new FieldMessage(fieldConfig.getElementId(), index, "Please enter a value in " + fieldConfig.getLabel());
                fieldMessages.add(msg);
                allFieldMessages.add(msg);
            }
        }
        value.setFieldMessages(fieldMessages);
        if (type.hasChildren && !type.hasIndexedChildren && value.getChildNode() != null) {
            for (String key : value.getChildNode().keySet()) {
                DynFormFieldDTO child = value.getChildNode().getField(key);
                DynFormFieldConfigDTO childConfig = fieldConfig.getChildren().getFieldConfig(key);
                allFieldMessages.addAll(this.validateField(childConfig, child, 0));
            }
        } else if (type.hasChildren && type.hasIndexedChildren && value.getChildNodeList() != null) {
            int ix = 0;
            for (DynFormDataNodeDTO childNode : value.getChildNodeList()) {
                for (String key : childNode.keySet()) {
                    DynFormFieldDTO child = childNode.getField(key);
                    DynFormFieldConfigDTO childConfig = fieldConfig.getChildren().getFieldConfig(key);
                    allFieldMessages.addAll(this.validateField(childConfig, child, ix));
                }
                ix++;
            }
        }
        return allFieldMessages;
    }

    private void createVals(List<DynFormValueEntity> vals, DynFormDataDTO form, DynFormValueEntity parent, DynFormDataNodeDTO data) {
        for (final String key : data.keySet()) {
            final DynFormFieldDTO value = data.getField(key);
            final DynFormValueEntity formVal = this.createFormValue(form, parent, key, value);
            vals.add(formVal);
        }
    }

    private DynFormValueEntity createFormValue(DynFormDataDTO form, DynFormValueEntity parent, String elementId, DynFormFieldDTO value) {
        final DynFormValueEntity formVal = new DynFormValueEntity();
        formVal.setId(this.generateId());
        formVal.setFormInstanceId(form.getId());
        formVal.setFormConfigId(form.getId());
        formVal.setElementId(elementId);
        if (parent != null) {
            formVal.setParentElementId(parent.getElementId());
            formVal.setParent(parent);
        }
        if (value.getChildNodeList() != null) {
            // this has children
            final List<DynFormDataNodeDTO> list = value.getChildNodeList();
            int index = 0;
            for (final DynFormDataNodeDTO childRow : list) {
                for (final String key : childRow.keySet()) {
                    final DynFormFieldDTO childVal = childRow.getField(key);
                    final DynFormValueEntity childFormVal = this.createFormValue(form, formVal, key, childVal);
                    childFormVal.setIndex(index);
                    formVal.getChildren().add(childFormVal);
                }
                index++;
            }
        } else if (value.getChildNode() != null) {
            // this has children
            final DynFormDataNodeDTO childRow = value.getChildNode();
            for (final String key : childRow.keySet()) {
                final DynFormFieldDTO childVal = childRow.getField(key);
                final DynFormValueEntity childFormVal = this.createFormValue(form, formVal, key, childVal);
                childFormVal.setIndex(null);
                formVal.getChildren().add(childFormVal);
            }
        } else {
            String sVal = value.getValue() == null ? null : String.valueOf(value.getValue());
            formVal.setStringValue(sVal);
        }
        return formVal;
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }

//    public List<DynFormFieldValueLineItemDTO> searchForValues(GenericSearchInput searchInput) {
//        List<DynFormValue> values = this.repo.findByGenericSearchInput(searchInput, 10000);
//
//        List<DynFormFieldValueLineItemDTO> result = new ArrayList<DynFormFieldValueLineItemDTO>(
//                values.size());
//        for (DynFormValue v : values) {
//            DynFormFieldValueLineItemDTO dto = new DynFormFieldValueLineItemDTO();
//            dto.setElementId(v.getElementId());
//            dto.setFormInstanceId(v.getFormInstanceId());
//            dto.setFormName(v.getFormName());
//            dto.setId(v.getId());
//            dto.setIndex(v.getIndex());
//            dto.setParentElementId(v.getParentElementId());
//            dto.setStringValue(v.getStringValue());
//            result.add(dto);
//        }
//        return result;
//    }

//    public List<DynFormFieldValueLineItemDTO> searchForValues(
//            String form,
//            Map<String, String> searchValues
//    ) {
//        GenericSearchInput mainInput = new GenericSearchInput();
//        mainInput.AND(new GenericSearchInput("formName", Option.EQ, form));
//
//        GenericSearchInput attrInputs = new GenericSearchInput();
//
//        for (String fieldName : searchValues.keySet()) {
//            String fieldValue = searchValues.get(fieldName);
//            String likeValue = fieldValue;
//            likeValue = likeValue.replace("*", "%");
//            //likeValue = "%" + likeValue +"%";
//            GenericSearchInput attr = new GenericSearchInput();
//            attr.AND(new GenericSearchInput("elementId", Option.EQ, fieldName));
//            attr.AND(new GenericSearchInput("stringValue", Option.LIKE, likeValue));
//            attrInputs.OR(attr);
//        }
//        mainInput.AND(attrInputs);
//        return this.searchForValues(mainInput);
//    }
//
//    public List<DynFormFieldValueLineItemDTO> searchForValues(
//            String form,
//            DynFormDataDTO searchFormData
//    ) {
//
//        Map<String, String> searchValues = new LinkedHashMap<String, String>();
//
//        for (DynFormFieldDTO field : searchFormData.getData()
//                .getAllFieldsIncludingChildrenFields()) {
//            String value = field.getValueAsString();
//            String key = field.getElementId();
//            searchValues.put(key, value);
//        }
//        return this.searchForValues(form, searchValues);
//    }
//
//    public static void main(String[] args) throws JsonProcessingException {
//
//        final DynFormDataService service = new DynFormDataService();
//        final DynFormDataDTO formData = service.getTestFormData();
//
//        final List<DynFormValue> vals = new ArrayList<DynFormValue>();
//        service.createVals(vals, formData, null, formData.getData());
//        for (final DynFormValue val : vals) {
//            System.out.println(val);
//        }
//    }
}
