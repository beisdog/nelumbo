package com.deep.nelumbo.dynform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that contains a key and value used in the ValueHelp.
 *
 * @author X200531
 */
@Data
@NoArgsConstructor
public class KeyValueDTO {

    String key;
    String value;

    public KeyValueDTO(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }
}
