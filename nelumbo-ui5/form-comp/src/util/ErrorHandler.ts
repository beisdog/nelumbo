import MessageBox from "sap/m/MessageBox";
import BusyIndicator from "sap/ui/core/BusyIndicator";
import UIComponent from "sap/ui/core/UIComponent";
import Dialog from "sap/m/Dialog";
import Text from "sap/m/Text";
import {ValueState} from "sap/ui/core/library";
import JSONModel from "sap/ui/model/json/JSONModel";

interface CustomJqXHRequestOption {
    type: string,
    url: string,
    data: any
}

interface CustomJQueryXHR extends JQueryXHR{
    message: string,
    originalRequestOptions: CustomJqXHRequestOption
}

export default class ErrorHandler {

    private oFormDataModel: JSONModel = null;

    constructor(private oComponent: UIComponent) {

    }

    public getText(sMessage: string): string {
        return sMessage;
        //this.oResourceModel.getResourceBundle().getText(sMessage);
    }

    public showGeneralErrorMessage(message: string): void {
        MessageBox.error(this.getText(message));
        console.info("Error: " + this.getText(message));
    }
    public showServerRespondedInvalidDataError(): void {
        var failureMsg = "The server responded invalid data. The requested action was aborted. Please call the system administrator.";
        var failureDialogTitle = "Error";
        MessageBox.error(failureMsg, { title: failureDialogTitle });
    }

    public showBlockingErrorMessage(errorMessage: string) {
        var busyDialog = new Dialog({
            title: "An error has happened",
            content: new Text({
                text: errorMessage
            }),
            state: ValueState.Error
        });
        busyDialog.open();
    }

    public handleError(pJqXHR: JQueryXHR, sTextStatus: string, sErrorThrown: string) {
        var jqXHR = pJqXHR as CustomJQueryXHR;
        console.error("handleError!");
        console.error(jqXHR.originalRequestOptions);
        console.error(jqXHR);
        console.error(sTextStatus);
        console.error(sErrorThrown);
        BusyIndicator.hide();
        var sMessage = null;
        if (jqXHR.responseText) {
            sMessage = jqXHR.responseText;
        }
        // set the validation errors if a form was sent
        if (this.oFormDataModel && jqXHR.responseJSON) {
            var json = jqXHR.responseJSON;
            if (json.text) {
                sMessage = json.text;
            }
            else if (jqXHR.responseJSON.message) {
                sMessage = json.message;
                if (json.data) {
                    this.oFormDataModel.setData(json.data);
                }
            }

        }
        if (!sMessage) {
            sMessage = sErrorThrown;
        }
        if (!sMessage) {
            sMessage = sTextStatus;
        }
        //no rest error but a javascript error
        if (!sMessage && jqXHR.message) {
            sMessage = jqXHR.message;
        }
        // add url that caused the error at the end
        if (jqXHR && jqXHR.originalRequestOptions) {
            let options = jqXHR.originalRequestOptions;
            sMessage += ". Url:" + options.type + " " + options.url + (options.data ? ("?" + options.data) : "");
        }
        this.showGeneralErrorMessage(sMessage);
    }

}
