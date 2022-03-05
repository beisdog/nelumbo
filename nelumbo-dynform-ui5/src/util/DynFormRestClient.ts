import RestClient, { StartDeferred } from "./RestClient";


export default class DynFormRestClient extends RestClient {

    public getFormConfig(sBrmDcName: string, sFormName: string, sTask: string): StartDeferred {
        var sUrl = this.baseUrl + "/util-dynform-rest/config/" + sBrmDcName + "/DynFormConfigRuleset/" + sFormName + "/" + sTask;
        return this._GET_DEFERRED(sUrl).start();
    }
    public getNewFormData(sBrmDcName: string, sFormName: string, sTask: string, sId: string): StartDeferred {
        //url: data/{id}/new/{brmDcName}/{rulesetNameDynForm}/{formName}/{task}
        var sUrl = this.baseUrl + "/util-dynform-rest/data/" + sId + "/new/" + sBrmDcName + "/DynFormConfigRuleset/" + sFormName + "/" + sTask;
        return this._GET_DEFERRED(sUrl).start();
    }
    public callOnChangeEvent(sSourceElementId: string, sRestUrl: string, sDataPath: string, oFormData: any, oFormConfig: any): StartDeferred {
        return this._POST_DEFERRED(this.baseUrl + sRestUrl, {
            sourceElementId: sSourceElementId,
            dataPath: sDataPath,
            formData: oFormData,
            formConfig: oFormConfig
        }).start();
    }
}