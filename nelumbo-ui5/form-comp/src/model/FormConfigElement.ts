
export enum FormConfigElementType {
    StringInpu5tField = "StringInputField",
    MultiInputField = "MultiInputField",
    Checkbox = "Checkbox",
    DateInputField = "DateInputField",
    Group = "Group",
    Table = "Table",
    Button = "Button",
    Toolbar = "Toolbar"
}
export interface KeyWithFormConfigElement {
    [key: string]: FormConfigElement;
}

export interface KeyValue {
    key: string;
    value: string;
}

export interface FormConfigElement {
    elementId?: string;
    label: string;
    type: string;
	enabled?: boolean;
	visible?: boolean;
    editable?: boolean;
    children?: KeyWithFormConfigElement;
    values?: Array<KeyValue>;
    valueProvider?: string;
}
