/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2022.
 */

package com.deep.nelumbo.dynform.controller;

import com.deep.nelumbo.dynform.dto.DynFormConfigNodeDTO;
import com.deep.nelumbo.dynform.dto.DynFormDataDTO;
import com.deep.nelumbo.dynform.dto.DynFormDataNodeDTO;
import com.deep.nelumbo.dynform.dto.FieldMessage;
import com.deep.nelumbo.dynform.service.DynFormDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/form-data")
public class FormDataController {

    private final DynFormDataService formDataService;

    @GetMapping("/{formConfigId}/{state}/{id}")
    public DynFormDataDTO getFormData(String id, String formConfigId, String state) {
        return formDataService.getFormData(id, formConfigId, state);
    }

    @PostMapping("/{formConfigId}/{state}/{id}")
    public DynFormDataDTO createNewFormAndSave(String id, String formConfigId, String state) {
        return formDataService.createNewFormAndSave(id, formConfigId, state);
    }

    @GetMapping("/{formConfigId}/{state}/{id}/new")
    public DynFormDataDTO newForm(String formConfigId, String state, String id) {
        return formDataService.newForm(id, formConfigId, state);
    }


    public DynFormDataNodeDTO newFormDataNode(DynFormConfigNodeDTO config) {
        return formDataService.newFormDataNode(config);
    }

    public DynFormDataDTO saveFormData(DynFormDataDTO dto, boolean validate) {
        return formDataService.saveFormData(dto, validate);
    }

    public List<FieldMessage> validateFormData(DynFormDataDTO dto) {
        return formDataService.validateFormData(dto);
    }

    public String generateId() {
        return formDataService.generateId();
    }
}
