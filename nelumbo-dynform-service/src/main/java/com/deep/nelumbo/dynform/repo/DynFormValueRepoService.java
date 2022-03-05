package com.deep.nelumbo.dynform.repo;

import com.deep.nelumbo.dynform.entity.DynFormValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DynFormValueRepoService extends JpaRepository<DynFormValue, String> {

}
