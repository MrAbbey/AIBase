window.ExtendNAObj = (function() {

    if(WadeNAObj) {
        // 扩展区域
    	var ExtendNAObj = (function(){
    		return {

    		    // 这个key生成的规则是：原生对象名_原生方法名
    			// 测试函数，后面的方法，请按照这个方法扩展自己的自定义的方法
    			JN_ShowMessage:function(string,callback) {
    				// 方法名，即配置中的name
    				var callbackKey = 'JN_ShowMessage';
    				WadeNAObj.storageCallback(callbackKey,callback);
                    WadeNAObj.execute(callbackKey,string);
    			},

    			// 分享应用
                JN_OpenDocument:function(callback) {
                    var callbackKey = 'JN_OpenDocument';
                    WadeNAObj.storageCallback(callbackKey,callback);
                    WadeNAObj.execute(callbackKey);
                },
    		};
    	})();


        // 拷贝ExtendNAObj到WadeNAObj
        //
        extendObj(WadeNAObj, ExtendNAObj);
        function extendObj(tag, obj){
            var target = tag || {};
            if (typeof target !== "object"){
                target = {};
            }
            if (typeof obj !== "object"){
                return target;
            }
            for(name in obj){
                var src = target[name], copy = obj[name];
                if(target === copy){
                    continue;
                }
                if(obj.hasOwnProperty(name) === true){
                    target[name] = copy;
                }
            }
            return target;
        }
    	return ExtendNAObj;
    }
})();
