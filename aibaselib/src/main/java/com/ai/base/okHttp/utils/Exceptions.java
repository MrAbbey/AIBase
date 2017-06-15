package com.ai.base.okHttp.utils;

/**
 * Created by song on 4/8/2016.
 */
public class Exceptions
{
    public static void illegalArgument(String msg, Object... params)
    {
        throw new IllegalArgumentException(String.format(msg, params));
    }


}
