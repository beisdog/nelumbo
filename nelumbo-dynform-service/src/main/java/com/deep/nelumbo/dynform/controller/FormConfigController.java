/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2022.
 */

package com.deep.nelumbo.dynform.controller;

import com.deep.nelumbo.dynform.dto.DynFormConfigDTO;
import com.deep.nelumbo.dynform.entity.DynFormConfigEntity;
import com.deep.nelumbo.dynform.service.DynFormConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/form-config")
public class FormConfigController {

    private final DynFormConfigService formConfigService;

    @GetMapping("/{id}/{state}")
    public DynFormConfigDTO getFormConfig(@PathVariable("id") String formConfigId, @PathVariable("state") String state) {
        return formConfigService.getFormConfig(formConfigId, state);
    }

    @PostMapping("/")
    public DynFormConfigEntity save(@RequestBody DynFormConfigDTO formConfig) {
        return formConfigService.save(formConfig);
    }
}
