package com.ai.base.okHttp.builder;


import com.ai.base.okHttp.request.PostStringRequest;
import com.ai.base.okHttp.request.RequestCall;

import okhttp3.MediaType;

/**
 * Created by song on 4/8/2016.
 */
public class PostStringBuilder extends OkHttpRequestBuilder<PostStringBuilder>
{
    private String content;
    private MediaType mediaType;


    public PostStringBuilder content(String content)
    {
        this.content = content;
        return this;
    }

    public PostStringBuilder mediaType(MediaType mediaType)
    {
        this.mediaType = mediaType;
        return this;
    }

    @Override
    public RequestCall build()
    {
        return new PostStringRequest(url, tag, params, headers, content, mediaType,id).build();
    }


}
