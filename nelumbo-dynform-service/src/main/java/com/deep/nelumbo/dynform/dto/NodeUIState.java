package com.deep.nelumbo.dynform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UI settings that apply to a row (e.g. one row can be removed, another
 * cannot).
 *
 * @author David
 */
@Data
@NoArgsConstructor
public class NodeUIState {

    private Boolean removeEnabled;

    public NodeUIState(Boolean removeEnabled) {
        super();
        this.removeEnabled = removeEnabled;
    }

    public NodeUIState withRemoveEnabled(Boolean removeEnabled) {
        this.removeEnabled = removeEnabled;
        return this;
    }
}
