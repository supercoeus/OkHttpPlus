package com.socks.okhttp.plus.builder;

import android.text.TextUtils;

import com.socks.okhttp.plus.OkHttpProxy;
import com.socks.okhttp.plus.callback.OkCallback;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaokaiqiang on 15/11/24.
 */
public class GetRequestBuilder extends RequestBuilder {

    public GetRequestBuilder url(String url) {
        this.url = url;
        return this;
    }

    public GetRequestBuilder setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public GetRequestBuilder addParams(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    public GetRequestBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public Call execute(Callback callback) {

        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url can not be null !");
        }

        Request.Builder builder = new Request.Builder().url(url);

        if (tag != null) {
            builder.tag(tag);
        }

        if (params != null && params.size() > 0) {
            url = appendParams(url, params);
        }

        Request request = builder.build();

        if (callback instanceof OkCallback) {
            ((OkCallback) callback).onStart();
        }

        Call call = OkHttpProxy.getInstance().newCall(request);
        call.enqueue(callback);
        return call;
    }

}
