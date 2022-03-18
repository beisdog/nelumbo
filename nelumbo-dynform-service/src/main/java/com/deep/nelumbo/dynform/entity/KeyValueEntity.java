/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2022.
 */

package com.deep.nelumbo.dynform.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "DYNFORM_KV")
public class KeyValueEntity {

    @Id
    private String id;
    private String type;
    private String key;
    private String value;
}
