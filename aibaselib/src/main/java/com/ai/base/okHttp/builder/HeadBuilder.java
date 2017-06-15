package com.ai.base.okHttp.builder;


import com.ai.base.okHttp.OkHttpUtils;
import com.ai.base.okHttp.request.OtherRequest;
import com.ai.base.okHttp.request.RequestCall;

/**
 * Created by song on 4/8/2016.
 */
public class HeadBuilder extends GetBuilder
{
    @Override
    public RequestCall build()
    {
        return new OtherRequest(null, null, OkHttpUtils.METHOD.HEAD, url, tag, params, headers,id).build();
    }
}
