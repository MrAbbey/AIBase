window.modular = (function() {
	// 扩展区域
	var modular = (function(){
		return {
		    // 这个key生成的规则是：方法名称，即wade-plugin.xml里的name字段
            // 注意不要重复
			// 测试函数，后面的方法，请按照这个方法扩展自己的自定义的方法
			// 目前支持传给原生端的参数只有一个，类型: string, array,JSONObject(java侧是JSONObject,OC侧是NSDictionary)
			JN_Test:function(string,callback) {
				var callbackKey = 'JN_Test';
				modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey,string);
			},

			//
            JN_JSONObj:function(obj,callback) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_JSONObj';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey,obj);
            },

            // 扩展自己的接口
            // 退出应用
            JN_Quit:function(appName,callback) {
                var callbackKey = 'JN_Quit';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey,appName);
            },

            // 分享应用链接到系统剪切板
            JN_Sharing:function(url,callback) {
                var callbackKey = 'JN_Sharing';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey,url);
            },

            // 打开文档
            JN_OpenDocument:function(url,callback) {
                var callbackKey = 'JN_OpenDocument';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey,url);
            },

            // 检查版本自动更新
            JN_CheckVersion:function(url,callback) {
                var callbackKey = 'JN_CheckVersion';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey,url);
            },

            // 获取版本号
            JN_VersionNumber:function(callback) {
                var callbackKey = 'JN_VersionNumber';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey);
            },

            // 启动loading
            JN_ShowLoading:function(text,callback) {
                var callbackKey = 'JN_ShowLoading';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey,text);
            },

            // 启动loading
            JN_DismissLoading:function(callback) {
                var callbackKey = 'JN_DismissLoading';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey);
            },

            // 提示语
            JN_ShowMessage:function(string) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_ShowMessage';
                modular.execute(callbackKey,string);
            },

            // 存储数据
            JN_SetValueWithKey:function(value,key) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_SetValueWithKey';
                modular.execute(callbackKey,[key,value]);
            },

            // 获取数据,通过回调返回数据
            JN_GetValueWithKey:function(key,callback) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_GetValueWithKey';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey,key);
            },

            // 指纹验证
            JN_Fingerprint:function(callback) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_Fingerprint';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey);
            },

            // 发送短信，第一个参数，手机号，第二个参数：短信内容
            JN_SMS:function(phoneNumber,content) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_SMS';
                modular.execute(callbackKey,[phoneNumber,content]);
            },

            // 拨打电话，参数，手机号
            JN_Telephone:function(phoneNumber) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_Telephone';
                modular.execute(callbackKey,phoneNumber);
            },

            // 发邮件
            //第一个参数收件人邮箱号
            //第二个参数邮件主题
            //第三个邮件正文
            JN_Email:function(address,subject,content) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_Email';
                modular.execute(callbackKey,[address,subject,content]);
            },

            // 打开浏览器，参数，网址
            JN_Brower:function(urlString) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_Brower';
                modular.execute(callbackKey,urlString);
            },

            // 打开相册选择图片，单张图片，图片数据通过base64回传
            JN_SelectPhoto:function(callback) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_SelectPhoto';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey);
            },

            // 打开系统相机拍照，图片，图片数据通过base64回传
            JN_Photograph:function(callback) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_Photograph';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey);
            },

            // 自定义证件拍照
            JN_TakeCertificate:function(callback) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_TakeCertificate';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey);
            },
            
            // 扫码
            JN_QRcode:function(callback) {
                // 方法名，即配置中的name
                var callbackKey = 'JN_QRcode';
                modular.storeCallback(callbackKey,callback);
                modular.execute(callbackKey);
            },
                     
		};
	})();


    // 下面部分不需要修改！！！！！！
	// 全局变量
	var callbackMap = {};
	top.callbackMap = top.callbackMap||{};
	modular.storeCallback=function(callbackKey,callback){
		if (callback) {
			callbackMap[callbackKey] = {callback:callback, iframeName:window !== window.top ? window.name : null};
		}
	}

	modular.callback=function(callbackKey,data) {
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

   modular.osName=function() {
        var u = window.navigator.userAgent;
        var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Linux') > -1; //android
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

   modular.execute=function(pluginName,param) {
         //var paramString = '{"pluginName":"'+pluginName+'","params":'+param+'}';
         var paramObj = null;
         if(param == null) {
            paramObj = {"pluginName":pluginName};
         } else {
            paramObj = {"pluginName":pluginName,"params":param};
         }

         if (paramObj == null) return;
         var paramString = JSON.stringify(paramObj);
         if (modular.osName() =='a'){
             // android
             // android都调用JN_EXECUTE由这个函数进行方法的转发
             top.modularHander.JN_EXECUTE(paramString);
         } else if (modular.osName() == 'i') {
             // iOS
             // iOS都在WKWebview的WKScriptMessageHandler中进行转发
             window.webkit.messageHandlers.modularHander.postMessage(paramString);
         }
   }

   return modular;
})();
