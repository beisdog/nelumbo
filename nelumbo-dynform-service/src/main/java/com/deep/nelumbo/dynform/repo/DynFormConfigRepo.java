/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2022.
 */

package com.deep.nelumbo.dynform.repo;

import com.deep.nelumbo.dynform.entity.DynFormConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DynFormConfigRepo extends JpaRepository<DynFormConfigEntity, String> {

}
