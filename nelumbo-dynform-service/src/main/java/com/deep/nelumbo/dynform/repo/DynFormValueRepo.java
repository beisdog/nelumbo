package com.deep.nelumbo.dynform.repo;

import com.deep.nelumbo.dynform.entity.DynFormValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynFormValueRepo extends JpaRepository<DynFormValueEntity, String> {

    List<DynFormValueEntity> findByIdAndFormConfigIdAndParentIsNull(String id, String formConfigId);

    List<DynFormValueEntity> findByFormInstanceId(String id);
}
