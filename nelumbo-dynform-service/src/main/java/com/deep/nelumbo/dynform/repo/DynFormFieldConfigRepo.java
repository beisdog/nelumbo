/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2022.
 */

package com.deep.nelumbo.dynform.repo;

import com.deep.nelumbo.dynform.entity.DynFormFieldConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DynFormFieldConfigRepo extends JpaRepository<DynFormFieldConfigEntity, String> {

    List<DynFormFieldConfigEntity> findByFormConfigIdAndState(String formConfigId, String state);
}
