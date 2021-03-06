package com.deep.nelumbo.dynform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity that contains the field values of a form instance.
 *
 * @author X200531
 */
@Getter
@Setter
@Entity
@Table(name = "DYNFORM_FIELD_VALUE")
public class DynFormValueEntity {

    /**
     * Generic id that uniquely identifies the db record.
     */
    @Id
    private String id;
    /**
     * The name of the form. E.g. LAC. This is just for easier querying the forms according to the
     * form name.
     */
    private String formInstanceId;

    private String formConfigId;

    /**
     * index if this is tabular data or null
     */
    private Integer index;

    @JsonIgnore
    @ManyToOne
    private DynFormValueEntity parent;

    /**
     * References the parent->elementId This is basically just to add readablity to the table.
     */
    private String parentElementId;

    private String elementId;

    private String stringValue;

    @OneToMany(targetEntity = DynFormValueEntity.class, mappedBy = "parent", cascade = CascadeType.ALL)
    private List<DynFormValueEntity> children = new ArrayList<DynFormValueEntity>();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final DynFormValueEntity other = (DynFormValueEntity) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DynFormValue [elementId=" + this.elementId + ", stringValue=" + this.stringValue
                + ", id=" + this.id + ", parentElementId="
                + this.parentElementId + ", index="
                + this.index + ", children=" + this.children + "]";
    }
}
