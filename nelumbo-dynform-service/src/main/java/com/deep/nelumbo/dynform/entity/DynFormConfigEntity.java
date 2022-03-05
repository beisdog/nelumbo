package com.deep.nelumbo.dynform.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "DYNFORM_CONFIG")
public class DynFormConfigEntity {

    @Id
    private String id;

    private String formName;

    @OneToMany
    private List<DynFormFieldConfig> fieldConfigs;
}
