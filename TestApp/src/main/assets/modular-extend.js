window.extendModular = (function() {

    if(modular) {
        // 扩展区域
    	var extendModular = (function(){
    		return {

    		    // 这个key生成的规则是：原生对象名_原生方法名
    			// 测试函数，后面的方法，请按照这个方法扩展自己的自定义的方法
    			JN_ShowMessage:function(string,callback) {
    				// 方法名，即配置中的name
    				var callbackKey = 'JN_ShowMessage';
    				modular.storeCallback(callbackKey,callback);
                    modular.execute(callbackKey,string);
    			},

    			// 分享应用
                JN_OpenDocument:function(url,callback) {
                    var callbackKey = 'JN_OpenDocument';
                    modular.storeCallback(callbackKey,callback);
                    modular.execute(callbackKey,url);
                },
    		};
    	})();


        // 拷贝ExtendNAObj到modular
        //
        extendObj(modular, extendModular);
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
                if(src === copy){
                    continue;
                }
                if(target.hasOwnProperty(name) === false){
                    target[name] = copy;
                }
            }
            return target;
        }
    	return extendModular;
    }
})();
