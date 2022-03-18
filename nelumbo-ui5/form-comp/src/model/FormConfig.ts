import {KeyWithFormConfigElement} from "./FormConfigElement";


export default interface FormConfig {
    id: string,
    state: string,
    elements: KeyWithFormConfigElement

}
