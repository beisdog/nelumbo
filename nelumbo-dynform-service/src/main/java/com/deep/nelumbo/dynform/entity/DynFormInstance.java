package com.deep.nelumbo.dynform.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * NOT NEEDED. Maybe for future use if the {@link DynFormFieldConfig} Snapshot is also saved.
 *
 * @author X200531
 */
@Getter
@Setter
@Entity
@Table(name = "DYNFORM_INST")
public class DynFormInstance {

    @Id
    private String id;
    /**
     * The name of the form. E.g. LAC.
     */
    private String formName;
    //@OneToMany(mappedBy = "formInstance", cascade = CascadeType.ALL)
    @Transient
    private List<DynFormFieldConfig> fieldConfigs = new ArrayList<DynFormFieldConfig>();
}
