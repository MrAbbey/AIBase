window.WadeNAObj = (function() {

	// 扩展区域
	var WadeNAObj = (function(){
		return {
		    // 这个key生成的规则是：原生对象名_原生方法名
			// 测试函数，后面的方法，请按照这个方法扩展自己的自定义的方法
			JN_Test:function(string,callback) {
				// actionName=methodName
				var callbackKey = 'JN_Test';
				WadeNAObj.storageCallback(callbackKey,callback);
                WadeNAObj.execute(callbackKey,string);
			},

            // 扩展自己的接口
		};
	})();


    // 下面部分不需要修改！！！！！！
	// 全局变量
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

   WadeNAObj.osName=function() {
        var u = window.navigator.userAgent;
        var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Linux') > -1; //g
        var isIOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端
        if (isAndroid) {
            //android操作系统
            return 'a';
        }
        if (isIOS) {
            //iOS操作系统
            return 'i';
        }
   }

   WadeNAObj.execute=function(methodName,param) {
         //var paramString = '{"methodName":"'+methodName+'","params":'+param+'}';
         var paramObj = {"methodName":methodName,"params":param};
         var paramString = JSON.stringify(paramObj);
         if (WadeNAObj.osName() =='a'){
             // android
             top.WadeNAObjHander.JN_EXECUTE(paramString);
         } else if (WadeNAObj.osName() == 'i') {
             // iOS
             window.webkit.messageHandlers.WadeNAObjHander.postMessage(paramString);
         }
   }

   return WadeNAObj;
})();
