import { PropertyBindingInfo } from "sap/ui/base/ManagedObject";
import Model from "sap/ui/model/Model";

export default class DynFormHelper {

	// functions
	constructor(private oFormDataModel: Model, private oFormConfigModel: Model) {

	}
	getShowValueHelpExpressionBinding(sCurrentDataPath: string, sCurrentConfigPath: string): PropertyBindingInfo {
		return {
			path: "{=("
				+ "(${formConfig>" + sCurrentConfigPath + "/values} "
				+ "|| ${formData>" + sCurrentDataPath + "/uiState/values} "
				+ " ? true: false)"
				+ ")}"
		};
	}

	/*getShowValueHelpExpressionBinding: function(sCurrentDataPath, sCurrentConfigPath) {
		return "{=("
			+ "${formData>" + sCurrentDataPath +"/uiState}.hasOwnProperty('values')?"
			+ "(${formData>" + sCurrentDataPath +"/uiState/values} !== null ?true: (${formConfig>" + sCurrentConfigPath +"/values} !== null? true: false)):"
			+ "(${formConfig>" + sCurrentConfigPath +"/values} !== null? true: false)" 
			+ ")}";	
	},*/
	getUiStateExpressionBinding(sCurrentDataPath: string, sCurrentConfigPath: string, sProperty: string): PropertyBindingInfo {
		return {
			path:
				"{= ("
				+ "${formData>" + sCurrentDataPath + "/uiState/" + sProperty + "} === null || ${formData>" + sCurrentDataPath + "/uiState/" + sProperty + "} === undefined"
				+ " ? "
				+ "${formConfig>" + sCurrentConfigPath + "/" + sProperty + "}"
				+ " : "
				+ "${formData>" + sCurrentDataPath + "/uiState/" + sProperty + "}"
				+ ")}"
		};
	}

	getUiStateExpressionBindingForRow(sCurrentConfigPath: string, sProperty: string, sConfigProperty: string): PropertyBindingInfo {
		return {
			path: "{= ("
				+ "${formData>uiState/" + sProperty + "} === null || ${formData>uiState/" + sProperty + "} === undefined"
				+ " ? "
				+ "${formConfig>" + sCurrentConfigPath + "/" + sConfigProperty + "}"
				+ " : "
				+ "${formData>uiState/" + sProperty + "}"
				+ ")}"
		};

		/*return "{= (${formData>uiState/" + sProperty + "} !== undefined ? "
			+ "${formData>uiState/" + sProperty + "} :  "
			+ "${formConfig>" + sCurrentConfigPath +"/" + sConfigProperty + "})}";	
			*/
	}

	////// END
	getFormFieldConfigByElementId(oElements: any, sElementId: string): any {
		if (oElements[sElementId]) {
			return oElements[sElementId];
		}
		for (var sKey in oElements) {
			var child = oElements[sKey];
			var oFieldConfig = this.getFormFieldConfigByElementId(child.children, sElementId);
			if (oFieldConfig != null) {
				return oFieldConfig;
			}
		}
		return null;
	}
}