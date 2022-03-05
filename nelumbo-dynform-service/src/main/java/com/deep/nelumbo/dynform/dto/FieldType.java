package com.deep.nelumbo.dynform.dto;

/**
 * Enumeration for the different UI elements in a form.
 * 
 * @author X200531
 * 
 */
public enum FieldType {

	StringInputField(false, false), 
	DateInputField(false, false), 
	MultiInputField(false, false),
	Checkbox(false, false, null), 
    Group(true, false), 
	Table(true, true), 
	ExternalConfig(false,false),
	Tab(true, false),
	Button(false,false),
	Toolbar(true, false),
	//TODO: implement in JS
	HorizontalLayout(true,false);

	public final boolean hasChildren;
	public final boolean hasIndexedChildren;
	public final Object defaultValue;

	private FieldType(boolean hasChildren, boolean hasIndexedChildren) {
		this.hasChildren = hasChildren;
		this.hasIndexedChildren = hasIndexedChildren;
		this.defaultValue = null;
	}
	private FieldType(boolean hasChildren, boolean hasIndexedChildren, Object defaultValue) {
		this.hasChildren = hasChildren;
		this.hasIndexedChildren = hasIndexedChildren;
		this.defaultValue = defaultValue;
	}

}
