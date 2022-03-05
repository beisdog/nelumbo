package com.deep.nelumbo.dynform.service;

import com.deep.nelumbo.dynform.dto.DynFormDataDTO;
import com.deep.nelumbo.dynform.repo.DynFormValueRepoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.merckgroup.mdm.material.util.commons.domain.GenericSearchInput;
import com.merckgroup.mdm.material.util.commons.domain.GenericSearchInput.Option;
import com.merckgroup.util.dynform.dto.*;
import com.merckgroup.util.dynform.entity.DynFormValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.ejb.EJBException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Responsible for reading and writing the form data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynFormDataService {

    private static final String UTIL_DYNFRM_VALS_SEQ = "UTIL_DYNFRM_VALS_SEQ";

    private final DynFormConfigService formConfigService;
    private final EntityManager entityManager;
    private final DynFormValueRepoService repo;

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

    @Override
    public DynFormDataDTO getFormData(
            String id,
            RuleLocationConfigDynForm parameterObject,
            String task
    ) {
        DynFormConfigDTO formConfig = this.formConfigService.getFormConfig(parameterObject, task);
        return this.getFormData(id, formConfig);
    }

    @Override
    public DynFormDataDTO getFormData(String id, DynFormConfigDTO formConfig) {
        final DynFormDataDTO result = new DynFormDataDTO();
        final Query query = this.entityManager
                .createQuery("SELECT c FROM " + DynFormValue.class.getSimpleName()
                        + " c WHERE c.formInstanceId=:id AND c.formName=:formName AND c.parent IS NULL ORDER BY c.id");
        query.setParameter("id", id);
        query.setParameter("formName", formConfig.getId());

        final List<DynFormValue> topLevel = query.getResultList();
        result.setFormConfigParameter(formConfig.getFormConfigParameter());
        result.setId(id);
        result.setTask(formConfig.getTask());

        final DynFormDataNodeDTO data = this.createNodeFromDynFormValues(
                result,
                formConfig.getElements(),
                topLevel
        );

        result.setData(data);
        // TODO check if the other way works
        // for (final DynFormValue val : topLevel) {
        // fillDataNodeFromDynFormValue(formConfig, data, val);
        // }
        return result;
    }

    private DynFormDataNodeDTO createNodeFromDynFormValues(
            DynFormDataDTO form,
            DynFormConfigNodeDTO configNode,
            Collection<DynFormValue> topLevel
    ) {
        //First create node, with all empty values
        DynFormDataNodeDTO dataNode = DynFormDataNodeDTO.createFromConfigNode(configNode);
        // fill the values
        for (final DynFormValue val : topLevel) {
            DynFormFieldConfigDTO fieldConfig = configNode.getFieldConfig(val.getElementId());
            this.setDynFormValue(form, dataNode, val, fieldConfig);
        }

        return dataNode;
    }

    private void setDynFormValue(
            DynFormDataDTO form,
            DynFormDataNodeDTO dataNode,
            DynFormValue val,
            DynFormFieldConfigDTO fieldConfig
    ) {
        final String key = val.getElementId();
        final DynFormFieldDTO valueDTO = new DynFormFieldDTO();
        valueDTO.setElementId(key);
        dataNode.setField(key, valueDTO);
        if (fieldConfig == null) {
            LOGGER.warningT("FieldValue:" + val.getElementId()
                    + " has no corresponding FieldConfiguration. Form " + form.getId() + "/" + form
                    .getTask());
        }
        if (fieldConfig != null && fieldConfig.getChildren() != null && FieldType
                .valueOf(fieldConfig.getType()).hasChildren) {
            FieldType fieldType = FieldType.valueOf(fieldConfig.getType());
            boolean hasIndex = fieldType == null ? false : fieldType.hasIndexedChildren;
            //			for (final DynFormValue child : val.getChildren()) {
            //				if (child.getIndex() != null) {
            //					hasIndex = true;
            //					break;
            //				}
            //			}
            DynFormConfigNodeDTO configNode = fieldConfig.getChildren();
            if (hasIndex) {
                DynFormDataNodeDTO childNode = null;
                final Map<Integer, DynFormDataNodeDTO> childNodeMap = new TreeMap<Integer, DynFormDataNodeDTO>();
                for (final DynFormValue childVal : val.getChildren()) {
                    final Integer index = childVal.getIndex();
                    childNode = childNodeMap.get(index);
                    if (childNode == null) {
                        childNode = DynFormDataNodeDTO.createFromConfigNode(configNode);
                        childNodeMap.put(index, childNode);
                    }
                    DynFormFieldConfigDTO childConfig = configNode
                            .getFieldConfig(childVal.getElementId());
                    this.setDynFormValue(form, childNode, childVal, childConfig);
                }
                List<DynFormDataNodeDTO> childList = new ArrayList<DynFormDataNodeDTO>(childNodeMap
                        .values());
                valueDTO.setChildNodeList(childList);
            } else {
                final DynFormDataNodeDTO childNode = DynFormDataNodeDTO
                        .createFromConfigNode(configNode);
                for (final DynFormValue childVal : val.getChildren()) {
                    DynFormFieldConfigDTO childConfig = configNode
                            .getFieldConfig(childVal.getElementId());
                    this.setDynFormValue(form, childNode, childVal, childConfig);
                }
                valueDTO.setChildNode(childNode);
            }
        } else {
            valueDTO.setValue(val.getStringValue());
        }
    }

    //	private void fillDataNodeFromDynFormValue(DynFormConfigDTO formConfig, DynFormDataNodeDTO dataNode, DynFormValue val) {
    //		final String key = val.getElementId();
    //		final DynFormFieldDTO valueDTO = new DynFormFieldDTO();
    //		valueDTO.setElementId(key);
    //
    //		dataNode.setField(key, valueDTO);
    //
    //		if (val.getChildren() != null && val.getChildren().size() > 0) {
    //			boolean hasIndex = false;
    //			for (final DynFormValue child : val.getChildren()) {
    //				if (child.getIndex() != null) {
    //					hasIndex = true;
    //					break;
    //				}
    //			}
    //			if (hasIndex) {
    //				DynFormDataNodeDTO childNode = null;
    //				final Map<Integer, DynFormDataNodeDTO> childNodeMap = new TreeMap<Integer, DynFormDataNodeDTO>();
    //				for (final DynFormValue childVal : val.getChildren()) {
    //					final Integer index = childVal.getIndex();
    //					childNode = childNodeMap.get(index);
    //					if (childNode == null) {
    //						childNode = new DynFormDataNodeDTO();
    //						childNodeMap.put(index, childNode);
    //					}
    //					fillDataNodeFromDynFormValue(formConfig, childNode, childVal);
    //				}
    //				List<DynFormDataNodeDTO> childList = new ArrayList<DynFormDataNodeDTO>(childNodeMap.values());
    //				valueDTO.setChildList(childList);
    //			} else {
    //				final DynFormDataNodeDTO childNode = new DynFormDataNodeDTO();
    //				for (final DynFormValue childVal : val.getChildren()) {
    //					fillDataNodeFromDynFormValue(formConfig, childNode, childVal);
    //				}
    //				valueDTO.setchildNode(childNode);
    //			}
    //		} else {
    //			valueDTO.setValue(val.getStringValue());
    //		}
    //	}

    /**
     * Create a new form and store it with empty values in the db.
     *
     * @param formName
     * @param task
     * @return the created form
     */
    @Override
    public DynFormDataDTO createNewFormAndSave(
            String formInstanceId,
            RuleLocationConfigDynForm parameterObject,
            String task
    ) {
        final DynFormConfigDTO formConfig = this.formConfigService
                .getFormConfig(parameterObject, task);
        return this.createNewFormAndSave(formInstanceId, formConfig);
    }

    @Override
    public DynFormDataDTO createNewFormAndSave(
            String formInstanceId,
            final DynFormConfigDTO formConfig
    ) {
        final DynFormDataDTO form = new DynFormDataDTO();
        form.setId(formInstanceId);
        form.setFormConfigParameter(formConfig.getFormConfigParameter());
        form.setTask(formConfig.getTask());
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
    @Override
    public DynFormDataDTO newForm(
            String formInstanceId,
            RuleLocationConfigDynForm parameterObject,
            String task
    ) {
        final DynFormConfigDTO formConfig = this.formConfigService
                .getFormConfig(parameterObject, task);
        return this.newForm(formInstanceId, formConfig);
    }

    @Override
    public DynFormDataDTO newForm(String formInstanceId, final DynFormConfigDTO formConfig) {
        final DynFormDataDTO form = new DynFormDataDTO();
        form.setId(formInstanceId);
        form.setFormConfigParameter(formConfig.getFormConfigParameter());
        form.setTask(formConfig.getTask());
        final DynFormDataNodeDTO data = new DynFormDataNodeDTO();
        form.setData(data);

        final DynFormConfigNodeDTO elements = formConfig.getElements();
        DynFormDataNodeDTO.initFromConfigNode(data, elements);
        form.setData(data);

        return form;
    }

    @Override
    public DynFormDataNodeDTO newFormDataNode(DynFormConfigNodeDTO config) {
        DynFormDataNodeDTO result = new DynFormDataNodeDTO();
        DynFormDataNodeDTO.initFromConfigNode(result, config);
        return result;
    }

    @Override
    public DynFormDataDTO saveFormData(DynFormDataDTO dto, boolean validate) {
        // delete former values and recreate them
        if (dto.getId() != null) {
            final Query query = this.entityManager.createQuery(
                    "SELECT c FROM " + DynFormValue.class.getSimpleName()
                            + " c WHERE c.formInstanceId=:id");
            query.setParameter("id", dto.getId());
            final List<DynFormValue> list = query.getResultList();
            for (final DynFormValue dynFormValue : list) {
                this.entityManager.remove(dynFormValue);
            }
        }
        // recreate the values
        final DynFormDataNodeDTO data = dto.getData();
        final List<DynFormValue> vals = new ArrayList<DynFormValue>();
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
        for (final DynFormValue val : vals) {
            this.entityManager.persist(val);
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
        DynFormConfigDTO config = this.formConfigService
                .getFormConfig(dto.getFormConfigParameter(), dto.getTask());
        List<FieldMessage> allFieldMessages = new ArrayList<FieldMessage>();
        for (String key : config.getElements().keySet()) {
            DynFormFieldDTO value = data.getField(key);
            if (value != null) {
                allFieldMessages
                        .addAll(this
                                .validateField(config.getElements().getFieldConfig(key), value, 0));
            }
        }
        return allFieldMessages;
    }

    private List<FieldMessage> validateField(
            DynFormFieldConfigDTO fieldConfig,
            DynFormFieldDTO value,
            int index
    ) {
        List<FieldMessage> allFieldMessages = new ArrayList<FieldMessage>();
        List<FieldMessage> fieldMessages = new ArrayList<FieldMessage>();

        if (fieldConfig == null) {
            return allFieldMessages;
        }
        FieldType type = FieldType.valueOf(fieldConfig.getType());

        if (fieldConfig.getRequired()) {
            String sValue =
                    value.getValue() == null ? sValue = "" : String.valueOf(value.getValue());
            if (sValue.trim().length() == 0) {
                FieldMessage msg = new FieldMessage(
                        fieldConfig.getElementId(),
                        index,
                        "Please enter a value in " + fieldConfig.getLabel()
                );
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
        } else if (type.hasChildren && type.hasIndexedChildren
                && value.getChildNodeList() != null) {
            int ix = 0;
            for (DynFormDataNodeDTO childNode : value.getChildNodeList()) {
                for (String key : childNode.keySet()) {
                    DynFormFieldDTO child = childNode.getField(key);
                    DynFormFieldConfigDTO childConfig = fieldConfig.getChildren()
                            .getFieldConfig(key);
                    allFieldMessages.addAll(this.validateField(childConfig, child, ix));
                }
                ix++;
            }
        }
        return allFieldMessages;
    }

    private void createVals(
            List<DynFormValue> vals,
            DynFormDataDTO form,
            DynFormValue parent,
            DynFormDataNodeDTO data
    ) {
        for (final String key : data.keySet()) {
            final DynFormFieldDTO value = data.getField(key);
            final DynFormValue formVal = this.createFormValue(form, parent, key, value);
            vals.add(formVal);
        }
    }

    private DynFormValue createFormValue(
            DynFormDataDTO form,
            DynFormValue parent,
            String elementId,
            DynFormFieldDTO value
    ) {
        final DynFormValue formVal = new DynFormValue();
        formVal.setId(this.generateId());
        formVal.setFormInstanceId(form.getId());
        formVal.setFormName(form.getFormConfigParameter().getFormName());
        formVal.setElementId(elementId);
        if (parent != null) {
            formVal.setParentElementId(parent.getElementId());
            formVal._setParent(parent);
        }
        if (value.getChildNodeList() != null) {
            // this has children
            final List<DynFormDataNodeDTO> list = value.getChildNodeList();
            int index = 0;
            for (final DynFormDataNodeDTO childRow : list) {
                for (final String key : childRow.keySet()) {
                    final DynFormFieldDTO childVal = childRow.getField(key);
                    final DynFormValue childFormVal = this
                            .createFormValue(form, formVal, key, childVal);
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
                final DynFormValue childFormVal = this
                        .createFormValue(form, formVal, key, childVal);
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
        return String.valueOf(this.getNextId(UTIL_DYNFRM_VALS_SEQ));
    }

    public Long getNextId(String sequenceName) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = this.dataSource.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select " + sequenceName + ".nextval from dual");
            rs.next();
            Long result = rs.getLong(1);
            return result;
        } catch (SQLException se) {
            throw new EJBException(se);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    @Override
    public List<DynFormFieldValueLineItemDTO> searchForValues(GenericSearchInput searchInput) {
        List<DynFormValue> values = this.repo.findByGenericSearchInput(searchInput, 10000);

        List<DynFormFieldValueLineItemDTO> result = new ArrayList<DynFormFieldValueLineItemDTO>(
                values.size());
        for (DynFormValue v : values) {
            DynFormFieldValueLineItemDTO dto = new DynFormFieldValueLineItemDTO();
            dto.setElementId(v.getElementId());
            dto.setFormInstanceId(v.getFormInstanceId());
            dto.setFormName(v.getFormName());
            dto.setId(v.getId());
            dto.setIndex(v.getIndex());
            dto.setParentElementId(v.getParentElementId());
            dto.setStringValue(v.getStringValue());
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<DynFormFieldValueLineItemDTO> searchForValues(
            String form,
            Map<String, String> searchValues
    ) {
        GenericSearchInput mainInput = new GenericSearchInput();
        mainInput.AND(new GenericSearchInput("formName", Option.EQ, form));

        GenericSearchInput attrInputs = new GenericSearchInput();

        for (String fieldName : searchValues.keySet()) {
            String fieldValue = searchValues.get(fieldName);
            String likeValue = fieldValue;
            likeValue = likeValue.replace("*", "%");
            //likeValue = "%" + likeValue +"%";
            GenericSearchInput attr = new GenericSearchInput();
            attr.AND(new GenericSearchInput("elementId", Option.EQ, fieldName));
            attr.AND(new GenericSearchInput("stringValue", Option.LIKE, likeValue));
            attrInputs.OR(attr);
        }
        mainInput.AND(attrInputs);
        return this.searchForValues(mainInput);
    }

    @Override
    public List<DynFormFieldValueLineItemDTO> searchForValues(
            String form,
            DynFormDataDTO searchFormData
    ) {

        Map<String, String> searchValues = new LinkedHashMap<String, String>();

        for (DynFormFieldDTO field : searchFormData.getData()
                .getAllFieldsIncludingChildrenFields()) {
            String value = field.getValueAsString();
            String key = field.getElementId();
            searchValues.put(key, value);
        }
        return this.searchForValues(form, searchValues);
    }

    public static void main(String[] args) throws JsonProcessingException {
        // final DynFormDTO formData = new
        // DynFormService().getFormData("1","LAC");
        // System.out.println("FormData: " + formData);
        //
        // String json2 = mapper.writeValueAsString(formData);
        // System.out.println(json2);

        final DynFormDataService service = new DynFormDataService();
        final DynFormDataDTO formData = service.getTestFormData();

        final List<DynFormValue> vals = new ArrayList<DynFormValue>();
        service.createVals(vals, formData, null, formData.getData());
        for (final DynFormValue val : vals) {
            System.out.println(val);
        }
    }
}
