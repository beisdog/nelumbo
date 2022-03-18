package com.deep.nelumbo.dynform.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Field configuration of the form at the time of creation of the form instance.
 * <p>
 * This is a copy of the {@link DynFormToField} persisted to the database.
 * NOT NEEDED AT THE MOMENT
 *
 * @author X200531
 */
@Getter
@Setter
@Entity
@Table(name = "DYNFORM_FIELD_CONFIG")
public class DynFormFieldConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String formConfigId;
    private String state;
    private String parentElementId;
    private Integer position;
    private String elementId;
    private String label;
    private String type;
    private Integer length;
    private String valueProvider;
    private Boolean visible;
    private Boolean enabled;
    private Boolean editable;
    private Boolean required;
}
