/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2022.
 */

package com.deep.nelumbo.dynform.repo;

import com.deep.nelumbo.dynform.entity.KeyValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeyValueRepo extends JpaRepository<KeyValueEntity, String> {

    List<KeyValueEntity> findByType(String type);

    KeyValueEntity findByTypeAndKey(String type, String key);

}
