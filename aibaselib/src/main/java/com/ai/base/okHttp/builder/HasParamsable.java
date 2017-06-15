package com.ai.base.okHttp.builder;

import java.util.Map;

/**
 * Created by song on 4/8/2016.
 */
public interface HasParamsable
{
    OkHttpRequestBuilder params(Map<String, String> params);
    OkHttpRequestBuilder addParams(String key, String val);
}
