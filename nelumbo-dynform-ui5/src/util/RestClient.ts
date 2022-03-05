import { Deferred } from "jquery";
import User from "./User";

export interface StartDeferred extends Deferred {
    start(): StartDeferred;
    mapResult?(data: any): any;
    mapError?(errorThrown: any): any;
}


export default class RestClient {

    private getMethod = "GET";
    private postMethod = "POST";

    public MOCK_USER: User; 
    private _baseUrl: string;

    get baseUrl(): string {
        return this._baseUrl;
    }
    set baseUrl(sUrl: string) {
        this._baseUrl = sUrl;
    }

    _processPostUrl(sUrl: string): string {
		if (this.MOCK_USER) {
			if (sUrl.indexOf("?")> -1) {
				sUrl = sUrl + "?user=" + this.MOCK_USER.userName;
			} else {
					sUrl = sUrl + "&user=" + this.MOCK_USER.userName;
			}
		}
		return sUrl;
	}

	_processGetData(sData: string) {
		if (this.MOCK_USER) {
			if (sData) {
				sData = sData + "&user=" + this.MOCK_USER.userName;
			} else {
					sData = "user=" + this.MOCK_USER.userName;
			}
		}
		return sData;
	}

  	_POST_DEFERRED(sUrl: string, oObject: any): StartDeferred {
  			sUrl = this._processPostUrl(sUrl);
			var def: StartDeferred = $.Deferred() as StartDeferred;
			var dataAccess = this;
			def.start = function() {
				$.ajax( 
                    sUrl,
                    {
					cache : false,
					url : sUrl,
					type : dataAccess.postMethod,
					dataType: "json",
					contentType: "application/json",
					async : true,
					data : dataAccess._encodeDataForPost(oObject),
					processData : true,
					success : function(data, textStatus, jxHR) {
						if(def.mapResult) {
							def.resolve(def.mapResult(data));
						}else {
							def.resolve(data);	
						}
					},
					error : function(jxHR, textStatus, errorThrown) {
						def.reject(jxHR, textStatus, errorThrown);
					}
				});
				return this;
			};
			return def;	
		}
	_encodeDataForPost(oObject: any): string {
		return JSON.stringify(oObject);
	}

	_MOCK_DEFERRED(oResult: any) {
		var def = $.Deferred();
		def.resolve(oResult);
		return def;
	}
	_GET_DEFERRED(sUrl: string, sData?: string): StartDeferred {
		var _this = this;
		var def = $.Deferred() as StartDeferred;
		var dataAccess = this;
		def.start = function() {
			$.ajax( {
				cache : false,
				url : sUrl,
				type : dataAccess.getMethod ,
				dataType: "json",
				contentType: "application/json",
				async : true,
				data : _this._processGetData(sData),
				processData : true,
				success : function(data, textStatus, jxHR) {
					if(def.mapResult) {
						def.resolve(def.mapResult(data));
					} 
					else {
						def.resolve(data);	
					}
				},
				error : function(jxHR, textStatus, errorThrown) {
					if(def.mapError) {
						def.reject(jxHR, textStatus, def.mapError(errorThrown));
					}
					else {
						def.reject(jxHR, textStatus, errorThrown);	
					}
				}
			});
			return this;
		};
		return def;
	}
	_encodeDataForGet(sData: string): string {
		return sData;
	}	
}