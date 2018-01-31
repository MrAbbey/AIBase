

window.WadeNAObj = (function() {

	// 扩展区域
	WadeNAObj = (function(){
		return {
			//测试函数，后面的方法，请按照这个方法扩展自己的自定义的方法
			JN_Test:function(string,callback) {
				// 这个key生成的规则是：原生对象名_原生方法名
				var callbackKey = 'PortalScriptPlugin_JN_Test';
				WadeNAObj.storageCallback(callbackKey,callback);

				// 原生对象和原生方法调用：原生穿梭过来的对象PortalScriptPlugin,调用的方法名：JN_Test
				top.PortalScriptPlugin.JN_Test(string);
			},
		};
	})();


	//全局变量
	var callbackMap = {};
	top.callbackMap = top.callbackMap||{};
	WadeNAObj.storageCallback=function(callbackKey,callback){
		if (callback) {
			callbackMap[callbackKey] = {callback:callback, iframeName:window !== window.top ? window.name : null};
		}
	}

	WadeNAObj.callback=function(callbackKey,data) {
		var callbackItem = callbackMap[callbackKey];
		if (callbackItem) {
			if (callbackItem.callback) {
		        var func = callbackItem.callback, iframeName = callbackItem.iframeName;
		        if(typeof func === "function"){
					if (iframeName !== null){
						top.callbackMap[func] = function(data){
							document[iframeName][func](data);
						};
						top.callbackMap[func](data);
					}else{
						func(data);
					}
	            }

	            if (callbackItem) {
			        delete callbackMap[callbackKey];
			        if (iframeName !== null){
			        	delete top.callbackMap[func];
			        }
			    }
			}
		}
	}

	return WadeNAObj;
})();