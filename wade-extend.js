window.ExtendNAObj = (function() {

    if(WadeNAObj) {
        // 扩展区域
    	var ExtendNAObj = (function(){
    		return {

    		    // 这个key生成的规则是：原生对象名_原生方法名
    			// 测试函数，后面的方法，请按照这个方法扩展自己的自定义的方法
    			JN_Test:function(string,callback) {
    				// 这个key生成的规则是：原生对象名_原生方法名
    				var callbackKey = 'ExtendScriptPlugin_JN_Test';
    				WadeNAObj.storageCallback(callbackKey,callback);

    				// 原生对象和原生方法调用：原生穿梭过来的对象ExtendScriptPlugin,调用的方法名：JN_Test
    				top.ExtendScriptPlugin.JN_Test(string);
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