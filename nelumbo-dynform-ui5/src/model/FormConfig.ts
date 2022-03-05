import { FormConfigElement, KeyWithFormConfigElement } from "./FormConfigElement";


export default interface FormConfig {
    formId: string,
    state: string,
    elements: KeyWithFormConfigElement

}