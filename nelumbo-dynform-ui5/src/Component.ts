import UIComponent from "sap/ui/core/UIComponent";
import { support } from "sap/ui/Device";
import JSONModel from "sap/ui/model/json/JSONModel";
import DynamicForm from "./controller/DynamicForm.controller";
import DynamicFormController from "./controller/DynamicForm.controller";
import DynFormRestClient from "./util/DynFormRestClient";
import ErrorHandler from "./util/ErrorHandler";
import RestClient from "./util/RestClient";



/**
 * @namespace com.deep.dynamicform
 */
export default class DynamicFormComponent extends UIComponent {

	public static metadata = {
		manifest: "json"
	};

	private contentDensityClass: string;
	private errorHandler: ErrorHandler;
	public restClient: DynFormRestClient;
	private controller: DynamicForm;
	private buildFormRequested: boolean;


	/**
	 * This method can be called to determine whether the sapUiSizeCompact or sapUiSizeCozy
	 * design mode class should be set, which influences the size appearance of some controls.
	 *
	 * @public
	 * @return {string} css class, either 'sapUiSizeCompact' or 'sapUiSizeCozy' - or an empty string if no css class should be set
	 */
	public getContentDensityClass(): string {
		if (this.contentDensityClass === undefined) {
			// check whether FLP has already set the content density class; do nothing in this case
			if (document.body.classList.contains("sapUiSizeCozy") || document.body.classList.contains("sapUiSizeCompact")) {
				this.contentDensityClass = "";
			} else if (!support.touch) { // apply "compact" mode if touch is not supported
				this.contentDensityClass = "sapUiSizeCompact";
			} else {
				// "cozy" in case of touch support; default for most sap.m controls, but needed for desktop-first controls like sap.ui.table.Table
				this.contentDensityClass = "sapUiSizeCozy";
			}
		}
		return this.contentDensityClass;
	}

	public init(): void {
		super.init();

		// create the views based on the url/hash
		this.getRouter().initialize();
		this.errorHandler = new ErrorHandler(this);
		// set the device model
		this.setModel(new JSONModel(), "uiConfig");
		this.setModel(new JSONModel(), "formConfig");
		this.setModel(new JSONModel(), "formData");

		this.restClient = new DynFormRestClient();
		

	}
	/**
	 * Called by the controller
	 */
	public async initWithDemoData(controller: DynamicForm) {
		this.controller = controller;
		let formConfigModel = new JSONModel();
		await formConfigModel.loadData("data/form.config.json");
		let formDataModel = new JSONModel();
		await formDataModel.loadData("data/form.data.json");
		this.setFormConfigModel(formConfigModel);
		this.setFormDataModel(formDataModel);
		this.buildForm();
	}

	public initWithNewForm(sBrmDcName: string, sFormConfigId: string, sTask: string, sFormDataId: string) {
		var oComponent = this;
		$.when(
			oComponent.restClient.getFormConfig(sBrmDcName, sFormConfigId, sTask),
			oComponent.restClient.getNewFormData(sBrmDcName, sFormConfigId, sTask, sFormDataId)
		)
			.then(function (oFormConfigData, oFormData) {
				var formConfigModel = new JSONModel();
				formConfigModel.setData(oFormConfigData);
				oComponent.setFormConfigModel(formConfigModel);

				var formDataModel = new JSONModel();
				formDataModel.setData(oFormData);
				oComponent.setFormDataModel(formDataModel);
				oComponent.buildForm();
			})
			.fail(this.handleError.bind(this))
			;

	}
	/**
	 * The form config model can be set from outside.
	 * After this the caller must also call buildForm.
	 * @public
	 */
	setUiConfigModel(jsonModel: JSONModel): void {
		this.setModel(jsonModel, "uiConfig");
	}
	/**
	 * The form config model can be set from outside.
	 * After this the caller must also call buildForm.
	 * @public
	 */
	setFormConfigModel(jsonModel: JSONModel): void {
		this.setModel(jsonModel, "formConfig");
	}
	/**
	 * This set the data model.
	 * This can happen anytime and no special care must be taken when this is called.
	 * @public
	 */
	setFormDataModel(jsonModel: JSONModel): void {
		console.info("DynamicFormComponent.setFormDataModel ENTER");
		this.setModel(jsonModel, "formData");
	}
	/**
	 * Called from outside once the formConfigModel was set
	 * the UI can be initialised. This method can only be called 
	 * once because when the UI is already initialised and this
	 * is called again this can lead to errors because i have not found a way yet
	 * to destroy the previously created UI elements.
	 * @public
	 */
	public buildForm(): void {
		console.info("DynamicFormComponent.buildForm ENTER");
		var oController = this.controller;
		if (oController) {
			oController.buildFormFromModel( this.getModel("formConfig")as JSONModel);
		} else {
			// avoid race conflict by setting this flag, which will then trigger the the buildForm later
			// This happens when the buildForm is called but the component controller is not initialized yet
			// which seems to be an IE problem.
			// therefore check this flag in the onControllerInitialized
			this.buildFormRequested = true;
		}
	}

	public showFieldMessages(arFieldMessages: Array<any>) {
		for (var i = 0; i < arFieldMessages.length; i++) {

		}
	}

	onControllerInitialized(oController: DynamicFormController) {
		this.controller = oController;
		// Just a sloppy workaround because it seems in IE the DynForm component is not 
		// properly initialized when the parent component requests it.
		if (this.buildFormRequested) {
			oController.buildFormFromModel(this.getModel("formConfig") as JSONModel);
		}
	}

	handleError(jqXHR: JQueryXHR, sTextStatus: string, sErrorThrown: string) {
		this.errorHandler.handleError(jqXHR, sTextStatus, sErrorThrown);

	}

}
