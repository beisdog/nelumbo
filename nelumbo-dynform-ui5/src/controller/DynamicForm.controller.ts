import Button, { $ButtonSettings } from "sap/m/Button";
import CheckBox from "sap/m/CheckBox";
import Column, { $ColumnSettings } from "sap/m/Column";
import ColumnListItem from "sap/m/ColumnListItem";
import DatePicker from "sap/m/DatePicker";
import Input, { $InputSettings } from "sap/m/Input";
import InputBase from "sap/m/InputBase";
import Label from "sap/m/Label";
import MessageBox, { Icon } from "sap/m/MessageBox";
import MultiInput, { $MultiInputSettings } from "sap/m/MultiInput";
import OverflowToolbar from "sap/m/OverflowToolbar";
import Page from "sap/m/Page";
import StandardListItem from "sap/m/StandardListItem";
import Table from "sap/m/Table";
import Token from "sap/m/Token";
import Toolbar from "sap/m/Toolbar";
import Event from "sap/ui/base/Event";
import { $CheckBoxSettings } from "sap/ui/commons/CheckBox";
import Control from "sap/ui/core/Control";
import Item from "sap/ui/core/Item";
import ListItem from "sap/ui/core/ListItem";
import Form from "sap/ui/layout/form/Form";
import FormContainer from "sap/ui/layout/form/FormContainer";
import FormElement from "sap/ui/layout/form/FormElement";
import ResponsiveGridLayout from "sap/ui/layout/form/ResponsiveGridLayout";
import Context from "sap/ui/model/Context";
import Filter from "sap/ui/model/Filter";
import FilterOperator from "sap/ui/model/FilterOperator";
import JSONModel from "sap/ui/model/json/JSONModel";
import DynamicFormComponent from "../Component";
import { FormConfigElement, FormConfigElementType, KeyValue, KeyWithFormConfigElement } from "../model/FormConfigElement";
import DynFormHelper from "../util/DynFormHelper";
import BaseController from "./BaseController";

/**
 * @namespace com.deep.dynamicform.controller
 */
export default class DynamicForm extends BaseController {

    private lastFormName: string = null;
    private formHelper: DynFormHelper = null;
    private  _valueHelpDialog: any = null;
    private inputField: Control = null;

    /**
     * Called when a controller is instantiated and its View controls (if
     * available) are already created. Can be used to modify the View before
     * it is displayed, to bind event handlers and do other one-time
     * initialization.
     * 
     * @memberOf DynFormComponent.DynFormComponent.view.view.DynamicForm
     */
    onInit(): void {
        this.formHelper = new DynFormHelper(this.getOwnerComponent().getModel("formData"), this.getOwnerComponent().getModel("formConfig"));
        //this.getOwnerComponent().onControllerInitialized(this);
        //todo remove
        this.getOwnerComponent().initWithDemoData(this);
    }
    /**
     * builds the form according to the formConfig model and formData model.
     */
    buildFormFromModel(oFormConfigModel: JSONModel) {
        if (oFormConfigModel.getData().formId !== undefined && oFormConfigModel.getData().formId != this.lastFormName) {
            try {
                let oPage = this.getView().byId("Page1") as Page;
                let oFormConfigData = oFormConfigModel.getData();
                this._buildElements(oPage, null, oFormConfigData.elements, "/data", "/elements");
                console.info("Page content: " + oPage.getContent().length);
                this.lastFormName = oFormConfigModel.getData().id;
            } catch (ex) {
                console.info(ex);
            }
        }
    }
    /**
     * Iterates through the formConfig elements and creates formfields for
     * each element. This method is recursive.
     * 
     * @param oElements:
     *            data object that describes the form fields. This is the
     *            elements node as you can see in the
     *            models/form.config.json
     * @param sParentDataPath -
     *            string that represents the bind path of formDataModel
     * @return sParentConfigPath - string that represents the bind path in
     *         the formConfigModel
     */
    _buildElements(oPage: Page, oContainer: FormContainer | Toolbar, oElements: KeyWithFormConfigElement, sParentDataPath: string, sParentConfigPath: string) {
        let oMyContainer = oContainer;
        let oLastFormElement = null;
        for (let sKey in oElements) {
            // We check if this key exists in the oElements
            if (oElements.hasOwnProperty(sKey)) {
                let sCurrentDataPath = sParentDataPath + "/" + sKey;
                let sCurrentConfigPath = sParentConfigPath + "/" + sKey;
                // someKey is only the KEY (string)! Use it to get the oElements:
                let oFormConfigElement = oElements[sKey];
                console.info("Process:" + sKey);
                try {
                    if (oFormConfigElement.type === FormConfigElementType.Group) {
                        oMyContainer = this._buildFormContainer(oPage, sKey, oFormConfigElement, sCurrentDataPath, sCurrentConfigPath);
                    } else if (oFormConfigElement.type === FormConfigElementType.Table) {
                        this._buildTable(oPage, sKey, oFormConfigElement, sCurrentDataPath, sCurrentConfigPath);
                    } else if (oFormConfigElement.type === FormConfigElementType.Toolbar) {
                        oMyContainer = this._buildToolbarBottom(oPage, sKey, oFormConfigElement, sCurrentDataPath, sCurrentConfigPath);
                    } else if (oFormConfigElement.type === FormConfigElementType.Button) {
                        let oBtn = this._buildButton(sKey, oFormConfigElement, sCurrentDataPath, sCurrentConfigPath);
                        if (oLastFormElement) {
                            oLastFormElement.addField(oBtn);
                        } else {
                            if (oMyContainer instanceof FormContainer) {
                                oLastFormElement = this._addFormElement(sKey, sCurrentDataPath, sCurrentConfigPath, oFormConfigElement, oMyContainer, oBtn);
                            } else if (oMyContainer instanceof Toolbar) {
                                oMyContainer.addContent(oBtn);
                                oLastFormElement = null;
                            } else {
                                throw new Error(oContainer + " is not a container, therefore the button cannot be added");
                            }
                        }
                    } else {
                        let oField = this._buildFormField(sKey, oFormConfigElement, sCurrentDataPath, sCurrentConfigPath);
                        oLastFormElement = this._addFormElement(sKey, sCurrentDataPath, sCurrentConfigPath, oFormConfigElement, oMyContainer as FormContainer, oField);
                    }
                } catch (ex) {
                    console.info("Error in " + sKey + " ex:" + ex);
                }
            }
        }
    }
    //Toolbar Footer
    _buildToolbarBottom(oPage: Page, sKey: string, oFormConfigElement: FormConfigElement, sCurrentDataPath: string, sCurrentConfigPath: string): OverflowToolbar {
        let oBar = new OverflowToolbar(sKey);
        oPage.addContent(oBar);
        if (oFormConfigElement.children) {
            this._buildElements(oPage, oBar, oFormConfigElement.children, sCurrentDataPath + "/childNode", sCurrentConfigPath + "/children");
        }
        return oBar;
    }

    //sKey, oFormConfigElement, sCurrentDataPath, sCurrentConfigPath
    _buildFormField(sKey: string, oFormConfigElement: FormConfigElement, sCurrentDataPath: string, sCurrentConfigPath: string): Control {
        let oField: InputBase | CheckBox = null;
        if (oFormConfigElement.type === FormConfigElementType.StringInputField || oFormConfigElement.type === FormConfigElementType.DateInputField) {
            oField = this._buildInputField(sKey, oFormConfigElement, sCurrentDataPath, sCurrentConfigPath);
        } else if (oFormConfigElement.type === FormConfigElementType.MultiInputField) {
            oField = this._buildMultiInputField(sKey, oFormConfigElement, sCurrentDataPath, sCurrentConfigPath);
        } else if (oFormConfigElement.type === FormConfigElementType.Checkbox) {
            oField = this._buildCheckbox(sKey, oFormConfigElement, sCurrentDataPath, sCurrentConfigPath);
        } else {
            console.info(oFormConfigElement.type + " not supported");
        }
        return oField;
    }
    /**
     * Creates the form containter if the type is "Group"
     */
    _buildFormContainer(oPage: Page, sKey: string, oFormConfigElement: any, sCurrentDataPath: string, sCurrentConfigPath: string): FormContainer {
        let oForm = new Form(sKey);
        let oLayout = new ResponsiveGridLayout(sKey + "_Layout");
        oLayout.setColumnsXL(4);
        oLayout.setColumnsL(3);
        oLayout.setColumnsM(2);
        oLayout.setEmptySpanL(0);
        oLayout.setEmptySpanM(0);
        oLayout.setEmptySpanXL(0);
        oLayout.setSingleContainerFullSize(true);
        oForm.setLayout(oLayout);
        oPage.addContent(oForm);

        let oContainer = new FormContainer(sKey + "_FormContainer", {
            title: oFormConfigElement.label
        });
        if (oFormConfigElement.children) {
            this._buildElements(oPage, oContainer, oFormConfigElement.children, sCurrentDataPath + "/childNode", sCurrentConfigPath + "/children");
        }
        oForm.addFormContainer(oContainer);
        return oContainer;
    }
    /**
     * Creates and adds a formelement to the formcontainer.
     * @return FormElement
     */
    _addFormElement(sKey: string, sCurrentDataPath: string, sCurrentConfigPath: string, oFormConfigElement: FormConfigElement, oFormContainer: FormContainer, oField: Control) {

        let oFormElement = null;
        //it is a formcontainer:
        if (oFormContainer.addFormElement) {
            oFormElement = new FormElement(sKey + "_element", {
                label: oFormConfigElement.label,
                visible: this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "visible")
            });
            oFormElement.addField(oField);
            oFormContainer.addFormElement(oFormElement);
        }
        else {
            throw new Error(oFormContainer + " is not a form container");
        }
        return oFormElement;
    }

    /**
     * Builds the String or DateInputField element.
     * Returns a inputfield
     */
    _buildInputField(sKey: string, oFormConfigElement: FormConfigElement, sCurrentDataPath: string, sCurrentConfigPath: string): InputBase {
        let oSettings: $InputSettings = {
            value: { path: "formData>" + sCurrentDataPath + "/value"},
            valueStateText: {path:"formData>" + sCurrentDataPath + "/valueStateText"},
            valueState: { path: "formData>" + sCurrentDataPath + "/valueState" }
        };
        let oUiConfigData = (this.getOwnerComponent().getModel("uiConfig") as JSONModel).getData();
        let sBaseBindingPath = "{formConfig>" + sCurrentConfigPath;
        if (oUiConfigData[sKey]) {
            sBaseBindingPath = "{uiConfig>/" + sKey;
        }
    
        oSettings.enabled = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "enabled");
        oSettings.editable = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "editable");
        oSettings.required = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "required");
        oSettings.visible = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "visible");
        
        let oField = null;
        if (oFormConfigElement.type === FormConfigElementType.StringInputField) {
            oField = new Input(sKey, oSettings);
            oField.setMaxLength(255); //TODO now hardcoded, move it to the config
            // valueHelp
            oField.data("configValuesPath", sCurrentConfigPath + "/values");
            oField.data("dataPath", sCurrentDataPath);
            oField.attachValueHelpRequest(null, this.handleValueHelp, this);
            if (oFormConfigElement.values) {
                //TODO suggestions
                oField.setShowValueHelp(true);
                oField.attachValueHelpRequest(null, this.handleValueHelp, this);
                let oItemTemplate = new ListItem(
                    {
                        text: "{formConfig>key}",
                        additionalText: "{formConfig>value}"
                    }
                );
                oField.bindAggregation("suggestionItems",
                    {
                        path: sBaseBindingPath.substring(1) + "/values",
                        template: oItemTemplate,
                        templateShareable: false
                    }
                );
            }
        }
        else if (oFormConfigElement.type === FormConfigElementType.DateInputField) {
            oField = new DatePicker(sKey, oSettings);
            oField.setValueFormat("yyyy-MM-dd");
            oField.setDisplayFormat("yyyy-MM-dd");
        }

        this._addOnEventHandler(oField, oFormConfigElement, sCurrentDataPath);

        return oField;
    }
    _addOnEventHandler(oField: Control, oFormConfigElement: FormConfigElement, sCurrentDataPath: string) {
        if (oFormConfigElement.valueProvider !== undefined && oFormConfigElement.valueProvider !== null) {
            let valueProvider = oFormConfigElement.valueProvider;
            if (valueProvider.startsWith("OnEvent:")) {
                let arValueProvider = valueProvider.split(":");
                let arEvUrl = arValueProvider[1].split("=");
                let sEvent = arEvUrl[0];
                let sUrl = arEvUrl[1];

                oField.data("restUrl", sUrl);
                oField.data("elementId", oFormConfigElement.elementId);
                oField.data("dataPath", sCurrentDataPath);

                if (sEvent == 'Change') {
                    if (oFormConfigElement.type === FormConfigElementType.Checkbox) { //applies to checkbox
                        (oField as CheckBox).attachSelect(null, this.handleInputFieldChangeEvent, this);
                    } else if (oField instanceof InputBase) {
                        (oField as InputBase).attachChange(null, this.handleInputFieldChangeEvent, this);
                    } else {
                        console.info("Field " + oFormConfigElement.elementId + " does not support Change or Select event :" + oField);
                    }
                } else if (sEvent == 'Enter') {
                    (oField as Input).attachSubmit(null, this.handleInputFieldChangeEvent, this);
                } if (sEvent == 'Click') {
                    (oField as Button).attachPress(null, this.handleInputFieldChangeEvent, this);
                } else {
                    console.info("Event type " + sEvent + " not supported in field " + oFormConfigElement.elementId);
                }
            }
        }
    }

    _buildCheckbox(sKey: string, oFormConfigElement: any, sCurrentDataPath: string, sCurrentConfigPath: string): CheckBox {
        let oSettings: $CheckBoxSettings = {
            checked: { path: "{formData>" + sCurrentDataPath + "/value}" },
            //Not supported by checkbox: valueStateText: "{formData>" + sCurrentDataPath + "/valueStateText}",
            valueState: { path: "{formData>" + sCurrentDataPath + "/valueState}" }
        };
        let oUiConfigData = (this.getOwnerComponent().getModel("uiConfig") as JSONModel).getData();
        // let sBaseBindingPath = "{formConfig>" + sCurrentConfigPath;
        // if (oUiConfigData[sKey]) {
        // 	sBaseBindingPath = "{uiConfig>/" + sKey ;
        // }
        oSettings.enabled = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "enabled");
        oSettings.editable = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "editable");
        oSettings.visible = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "visible");

        let oField = null;
        oField = new CheckBox(sKey, oSettings);

        this._addOnEventHandler(oField, oFormConfigElement, sCurrentDataPath);
        return oField;
    }

    _buildButton(sKey: string, oFormConfigElement: any, sCurrentDataPath: string, sCurrentConfigPath: string): Button {
        let oSettings: $ButtonSettings = {
            //text: "{formConfig>" + sCurrentDataPath + "/value}"//,
            //Not supported by checkbox: valueStateText: "{formData>" + sCurrentDataPath + "/valueStateText}",
            //valueState: "{formData>" + sCurrentDataPath + "/valueState}"
        };
        let oUiConfigData = (this.getOwnerComponent().getModel("uiConfig") as JSONModel).getData();
        let sBaseBindingPath = "{formConfig>" + sCurrentConfigPath;
        if (oUiConfigData[sKey]) {
            sBaseBindingPath = "{uiConfig>/" + sKey;
        }
        oSettings.enabled = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "enabled");
        oSettings.visible = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "visible");

        //label
        oSettings.text = sBaseBindingPath + "/label}";

        let oField = null;
        oField = new Button(sKey, oSettings);

        this._addOnEventHandler(oField, oFormConfigElement, sCurrentDataPath);
        return oField;
    }

    _buildMultiInputField(sKey: string, oFormConfigElement: FormConfigElement, sCurrentDataPath: string, sCurrentConfigPath: string) {
        console.info("_buildMultiInputField - Key: " + sKey + " - CurrentConfigPath: " + sCurrentDataPath);
        let oSettings: $MultiInputSettings = {
            valueStateText: { path: "formData>" + sCurrentDataPath + "/valueStateText" },
            valueState: { path: "formData>" + sCurrentDataPath + "/valueState" }
        };
        let oUiConfigData = (this.getOwnerComponent().getModel("uiConfig") as JSONModel).getData();
        let sBaseBindingPath = "formConfig>" + sCurrentConfigPath;
        if (oUiConfigData[sKey]) {
            sBaseBindingPath = "uiConfig>/" + sKey;
        }

        oSettings.enabled = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "enabled");
        oSettings.editable = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "editable");
        oSettings.required = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "required");
        oSettings.visible = this.formHelper.getUiStateExpressionBinding(sCurrentDataPath, sCurrentConfigPath, "visible");

        let oField = new MultiInput(sKey, oSettings);
        oField.setShowSuggestion(true);
        //oField.setMaxTokens(100); //TODO now hardcoded, move it to the config
        // valueHelp
        if (oFormConfigElement.values) {
            oField.setStartSuggestion(0);
            oField.data("configValuesPath", sCurrentConfigPath + "/values");
            oField.data("dataPath", sCurrentDataPath);

            //oField.data("valuePath", sCurrentConfigPath + "/values");
            oField.setShowValueHelp(true);
            oField.setShowSuggestion(true);
            oField.attachValueHelpRequest(null, this.handleValueHelp, this);

            let oItemTemplate = new Item(
                {
                    key: "{formConfig>key}",
                    text: "{formConfig>value}"
                }
            );
            oField.bindAggregation("suggestionItems",
                {
                    path: sBaseBindingPath.substring(1) + "/values",
                    template: oItemTemplate,
                    templateShareable: false
                }
            );
            // set tokens
            let oFormConfigModel = this.getOwnerComponent().getModel("formConfig") as JSONModel;
            let oFormDataModel = this.getOwnerComponent().getModel("formData") as JSONModel;
            let sValues = oFormDataModel.getProperty(sCurrentDataPath + "/value");
            let arSuggestionValues = oFormConfigModel.getProperty(sCurrentConfigPath + "/values");
            let arTokens = this._multiInput_createTokensFromString(sValues, arSuggestionValues);
            oField.setTokens(arTokens);

            let that = this;
            //attach event handler
            let fncTokenChange = function (oControlEvent: Event) {
                let oMultiInput = oControlEvent.getSource() as MultiInput;
                let sValue = that._multiInput_createStringFromTokens(oMultiInput.getTokens());
                oFormDataModel.setProperty(sCurrentDataPath + "/value", sValue);
                oFormDataModel.refresh(true);
            };
            //Needed but deprecated after 1.46 -> TokenUpdate (which does not work for us)
            oField.attachTokenChange(
                fncTokenChange,
                this);
            //New event after 1.46. Does not work properly in current version
            oField.attachTokenUpdate(
                fncTokenChange,
                this);
        }
        return oField;
    }

    _multiInput_RemoveToken(arTokens: Array<Token>, oToken: Token) {
        return arTokens.filter(
            function (value, index, array) {
                return value.getKey() != oToken.getKey();
            }
        );
    }

    _multiInput_GetToken(arTokens: Array<Token>, oToken: Token) {
        let arResult = arTokens.filter(
            function (value, index, array) {
                return value.getKey() === oToken.getKey();
            }
        );
        if (arResult.length === 0) {
            return null;
        }
        return arResult[0];
    }

    _multiInput_HasToken(arTokens: Array<Token>, oToken: Token): boolean {
        return this._multiInput_GetToken(arTokens, oToken) != null;
    }

    _multiInput_addAndRemoveTokens(arOriginalTokens: Array<Token>, arAddedTokens: Array<Token>, arRemovedTokens: Array<Token>):Array<Token> {
        let arTokens = [].concat(arOriginalTokens);
        console.info(arTokens);
        for (let i in arAddedTokens) {
            let addToken = arAddedTokens[i];
            if (!this._multiInput_HasToken(arTokens, addToken)) {
                arTokens.push(addToken);
            }
        }
        for (let i in arRemovedTokens) {
            let removeToken = arRemovedTokens[i];
            if (this._multiInput_HasToken(arTokens, removeToken)) {
                arTokens = this._multiInput_RemoveToken(arTokens, removeToken);
            }
        }
        return arTokens;
    }
    _multiInput_createStringFromTokens(arTokens: Array<Token>): string {
        let sResult = "";
        for (let i in arTokens) {
            let oToken = arTokens[i];
            if (i === "0") { //TODO not sure about this one
                sResult = oToken.getKey();
            }
            else {
                sResult = sResult + ";" + oToken.getKey();
            }
        }
        return sResult;
    }

    _multiInput_createTokensFromString(sValues: string, arSuggestionValues: Array<KeyValue>): Array<Token> {
        let arValues: Array<string> = [];
        if (sValues) {
            arValues = sValues.split(";");
        }
        let arTokens = [];
        for (let i in arValues) {
            let sVal = arValues[i];
            let oObj = this._multiInput_findSuggestionValueByKey(sVal, arSuggestionValues);
            if (oObj) {
                let oToken = new Token({
                    key: oObj.key,
                    text: oObj.value
                });
                arTokens.push(oToken);
            }
        }
        return arTokens;
    }

    _multiInput_findSuggestionValueByKey(sKey: string, arSuggestionValues: Array<KeyValue>): KeyValue {
        let arResult = arSuggestionValues.filter(
            function (value, index, array) {
                return value.key === sKey;
            }
        );
        if (arResult.length === 0) {
            return null;
        }
        return arResult[0];
    }
    /**
     * Builds a table. 
     */
    _buildTable(oPage: Page, sKey: string, oFormConfigElement: any, sCurrentDataPath: string, sCurrentConfigPath: string) {
        let aTableColumns: Array<Column> = [];
        let aTableCells: Array<Control> = [];
        let oUiConfigData = (this.getOwnerComponent().getModel("uiConfig") as JSONModel).getData();

        let sBaseBindingPath = "formConfig>" + sCurrentConfigPath;
        if (oUiConfigData[sKey]) {
            sBaseBindingPath = "uiConfig>/" + sKey;
        }

        // go through table children
        let oChildren = oFormConfigElement.children;
        for (let sColKey in oChildren) {
            if (oChildren.hasOwnProperty(sColKey)) {
                let oChild = oChildren[sColKey];
                aTableColumns.push(new Column({
                    header: new Label({
                        text: oChild.label,
                        tooltip: oChild.label
                    })
                }));
                let oColumnField = this._buildFormField(sColKey, oChild, sColKey, sCurrentConfigPath + "/children/" + sColKey);
                aTableCells.push(
                    oColumnField
                    //this._buildInputField(sColKey, oChild, sColKey, sCurrentConfigPath + "/children/" + sColKey )
                );
            }
        }
        // add delete column
        let oDeleteColSettings: $ColumnSettings = {
            header: new Label({
                text: "Delete"
            })
        };
        if (oFormConfigElement.enabled !== undefined) {
            oDeleteColSettings.visible = { path: sBaseBindingPath + "/enabled}" };

        }
        aTableColumns.push(new Column(oDeleteColSettings));

        let oDeleteBtnSettings: $ButtonSettings = { icon: "sap-icon://decline" };
        if (oFormConfigElement.enabled !== undefined) {
            oDeleteBtnSettings.visible = { path: sBaseBindingPath + "/enabled"};
            oDeleteBtnSettings.enabled = this.formHelper.getUiStateExpressionBindingForRow(sCurrentConfigPath, "removeEnabled", "enabled");
            oDeleteBtnSettings.visible = this.formHelper.getUiStateExpressionBindingForRow(sCurrentConfigPath, "removeEnabled", "enabled");
        }
        let oDeleteBtn = new Button(sKey + "_deleteRowBtn", oDeleteBtnSettings);
        oDeleteBtn.attachPress(this.handleDeleteRow, this);
        aTableCells.push(oDeleteBtn);
        // Add button
        let oAddBtnSettings: $ButtonSettings = { icon: "sap-icon://add" };
        if (oFormConfigElement.enabled !== undefined) {
            oAddBtnSettings.enabled = { path: sBaseBindingPath + "/enabled}" };
            oAddBtnSettings.visible = { path: sBaseBindingPath + "/enabled}" };
        }
        let oAddBtn = new Button(sKey + "_addRowBtn", oAddBtnSettings);
        oAddBtn.attachPress(this.handleAddRow, this);
        oAddBtn.data("valuePath", sCurrentDataPath + "/childNodeList");
        oAddBtn.data("configPath", sCurrentConfigPath + "/children");

        let oTable = new Table(sKey, {
            infoToolbar: new Toolbar({
                content: [oAddBtn]
            }),
            columns: aTableColumns
        });
        oTable.setHeaderText(oFormConfigElement.label);
        oTable.bindItems(
            {
                path: "formData>" + sCurrentDataPath + "/childNodeList",
                template: new ColumnListItem({
                        cells: aTableCells
                    })
            }
        );
        oPage.addContent(oTable);
    }
    /**
     * Event handler of the Add Row button.
     */
    handleAddRow(oEvent: any) {
        let sDataPath = oEvent.getSource().data("valuePath");
        let sConfigPath = oEvent.getSource().data("configPath");
        //this.formHelper.addRow(sDataPath, sConfigPath);
        let oFormDataModel = this.getOwnerComponent().getModel("formData") as JSONModel;
        let oFormConfigModel = this.getOwnerComponent().getModel("formConfig") as JSONModel;
        let aList = oFormDataModel.getProperty(sDataPath);
        if (!aList) {
            //it is null, then we need to
            aList = [];

        }
        let newFormData: any = {};
        let children = oFormConfigModel.getProperty(sConfigPath);
        for (let elementId in children) {
            newFormData[elementId] = {
                value: null,
                childNodeList: null,
                childNode: null,
                fieldMessages: []
            };
        }
        aList.push(newFormData);
        oFormDataModel.setProperty(sDataPath, aList);
        oFormDataModel.refresh(true);
    }
    /**
     * Event handler of the delete row button.
     */
    handleDeleteRow(oEvent: any) {
        let sPath = oEvent.getSource().getBindingContext("formData").getPath();
        let oModel = this.getView().getModel("formData");
        let iLastSegmentPos = sPath.lastIndexOf("/");
        let sContainerPath = sPath.substring(0, iLastSegmentPos);
        let iIndex = sPath.substring(iLastSegmentPos + 1);
        let aBomList = oModel.getProperty(sContainerPath);
        aBomList.splice(iIndex, 1);
        oModel.refresh(true);
    }
    /**
     * Event handler for the valuehelp if the inputfield has a value help.
     */
    handleValueHelp(oEvent: Event) {
        let input = oEvent.getSource() as InputBase;
        let oContext = input.getBindingContext("formData");

        let sInputValue = input.getValue();
        let sConfigValuesPath = input.data("configValuesPath");
        let sDataPath = input.data("dataPath");
        // context is only defined when it is a table it seems
        if (oContext) {
            sDataPath = oContext.getPath() + "/" + sDataPath;
        }
        let sDataValuesPath = sDataPath + "/uiState/values";

        // remember the originating input field to write back the value later
        this.inputField = input;
        // create value help dialog
        if (!this._valueHelpDialog) {
            this._valueHelpDialog = sap.ui.xmlfragment("com.deep.dynamicform.view.ValueHelp", this);
            this.getView().addDependent(this._valueHelpDialog);
        }
        let oDialogModel = new JSONModel();
        oDialogModel.setData(
            {
                values: []
            }
        );
        this._valueHelpDialog.setModel(oDialogModel);
        let listItem = new StandardListItem({
            title: "{key}",
            description: "{value}"
        });
        let arValues = this.getView().getModel("formData").getProperty(sDataValuesPath);
        if (!arValues) {
            arValues = this.getView().getModel("formConfig").getProperty(sConfigValuesPath);
        }
        oDialogModel.setProperty("/values", arValues);
        this._valueHelpDialog.bindAggregation("items", "/values", listItem);
        this._valueHelpDialog.open(sInputValue);
    }
    /**
     * Event handler for the data enhancer on the inputfield change event
     */
    handleInputFieldChangeEvent(oEvent: any) {
        this.handleInputFieldChange(oEvent.getSource());
    }

    handleInputFieldChange(oInput: any) {
        let sRestUrl = oInput.data("restUrl");
        if (sRestUrl === null) {
            return;
        }
        let sElementId = oInput.data("elementId");
        let sDataPath = oInput.data("dataPath");
        //this if the element is inside a table, then the index is also important
        // therefore get the indexed dataPath
        let oContext = oInput.getBindingContext("formData");
        // context is only defined when it is a table it seems
        if (oContext) {
            sDataPath = oContext.getPath() + "/" + sDataPath;
        }

        let oFormDataModel = this.getOwnerComponent().getModel("formData") as JSONModel;
        let oFormConfigModel = this.getOwnerComponent().getModel("formConfig") as JSONModel;

        $.when((this.getOwnerComponent() as DynamicFormComponent).restClient.callOnChangeEvent(
            sElementId,
            sRestUrl,
            sDataPath,
            oFormDataModel.getData(),
            oFormConfigModel.getData()
        ))
            .then(function (oResponse) {
                let icon:Icon = null;
                if (oResponse.formData) {
                    oFormDataModel.setData(oResponse.formData);
                    let sMessage = "";
                    let arMessages = oResponse.messages;

                    if (arMessages) {
                        for (let i = 0; i < arMessages.length; i++) {
                            if (arMessages[i].type === 'Error') {
                                icon = Icon.ERROR;
                            }
                            if (icon !== Icon.ERROR && arMessages[i].type === 'Warning') {
                                icon = Icon.WARNING;
                            }
                            sMessage += (arMessages[i].type ? arMessages[i].type + ":" : "") + arMessages[i].message;
                        }
                    }
                    let oOverwrittenValues = oResponse.overwrittenValues;

                    for (let sProp in oOverwrittenValues) {
                        sMessage += sProp + " is overwritten with " + oOverwrittenValues[sProp] + ",";
                    }
                    if (sMessage.length == 0) {
                        sMessage = "Operation finished!";
                    }
                    MessageBox.show(sMessage, {icon: icon});
                    //MessageToast.show(sMessage);
                } else {
                    MessageBox.show("The input of " + sElementId + " requested some data but nothing received", { icon: icon });
                    //MessageToast.show("The input of " + sElementId + " requested some data but nothing received");
                }
                if (oResponse.formConfig) {
                    oFormConfigModel.setData(oResponse.formConfig);
                }
            })
            .fail(this.getOwnerComponent().handleError.bind(this.getOwnerComponent()));

    }
    /**
     * Helper function that filters the items within the valuehelp dialog.
     */
    _handleValueHelpSearch(oEvent: any) {
        let sValue = oEvent.getParameter("value");
        let oFilter = new Filter("key", FilterOperator.Contains, sValue);
        oEvent.getSource().getBinding("items").filter([oFilter]);
    }
    /**
     * Event handler inside the Valuehelp that closes the dialog and applies the
     * selected element.
     */
    _handleValueHelpClose(oEvent: Event) {
        let oSelectedItem = oEvent.getParameter("selectedItem") ;
        if (oSelectedItem) {
            let oInputField = this.inputField as MultiInput;

            if (oInputField.addToken) {
                console.info(oSelectedItem);
                let oToken = new Token({ key: oSelectedItem.getTitle(), text: oSelectedItem.getDescription() });
                oInputField.addToken(oToken);
            }
            else {
                oInputField.setValue(oSelectedItem.getTitle());
            }
            //trigger the input change event
            this.handleInputFieldChange(oInputField);
        }
        (oEvent.getSource() as MultiInput).getBinding("items");//TODO: filter method does not exist???.filter([]);
    }
    /**
     *@memberOf DynFormComponent.controller.DynamicForm
     */
    handleComboxChange(oEvent: any) {
        let oSelectedItem = oEvent.getParameter("selectedItem");
        let sTask = oSelectedItem.getKey();
        let oFormConfigModel = new JSONModel();
        oFormConfigModel.loadData("/util-DynFormComponent-rest/config/LAC/" + sTask, {}, true);

        this.getView().setModel(oFormConfigModel, "formConfig");
    }

    handleLoad(oEvent: any) {
        let oFormDataModel = this.getView().getModel("formData") as JSONModel;
        let id = oFormDataModel.getData().id;
        oFormDataModel = new JSONModel();
        oFormDataModel.loadData("/util-dynform-rest/data/" + id + "/LAC/", {}, true);

        this.getView().setModel(oFormDataModel, "formData");
    }

}