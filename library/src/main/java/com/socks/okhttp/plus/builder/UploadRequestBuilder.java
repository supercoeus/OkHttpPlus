package com.socks.okhttp.plus.builder;

import android.util.Pair;

import com.socks.okhttp.plus.OkHttpProxy;
import com.socks.okhttp.plus.body.BodyWrapper;
import com.socks.okhttp.plus.listener.UploadListener;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaokaiqiang on 15/11/24.
 */
public class UploadRequestBuilder extends RequestBuilder {

    private static final int DEFAULT_TIME_OUT = 30;

    private int connectTimeOut;
    private int writeTimeOut;
    private int readTimeOut;
    private Pair<String, File> file;
    private Map<String, String> headers;

    public UploadRequestBuilder file(Pair<String, File> file) {
        this.file = file;
        return this;
    }

    public UploadRequestBuilder url(String url) {
        this.url = url;
        return this;
    }

    public UploadRequestBuilder setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public UploadRequestBuilder addParams(String key, String value) {
        if (params == null) {
            params = new IdentityHashMap<>();
        }
        params.put(key, value);
        return this;
    }

    public UploadRequestBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public UploadRequestBuilder addHeader(String key, String values) {
        if (headers == null) {
            headers = new IdentityHashMap<>();
        }
        headers.put(key, values);
        return this;
    }

    public UploadRequestBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    /**
     * @param connectTimeOut unit is minute
     * @return
     */
    public UploadRequestBuilder setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
        return this;
    }

    /**
     * @param writeTimeOut unit is minute
     * @return
     */
    public UploadRequestBuilder setWriteTimeOut(int writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
        return this;
    }

    /**
     * @param readTimeOut unit is minute
     * @return
     */
    public UploadRequestBuilder setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
        return this;
    }

    public Call start(UploadListener uploadListener) {

        MultipartBuilder multipartBuilder = new MultipartBuilder().type(MultipartBuilder.FORM);
        addParams(multipartBuilder, params);
        addFiles(multipartBuilder, file);

        Request.Builder builder = new Request.Builder();
        appendHeaders(builder, headers);
        Request request = builder.url(url).post(BodyWrapper.addProgressRequestListener(multipartBuilder.build(), uploadListener)).build();

        OkHttpClient clone = OkHttpProxy.getInstance().clone();

        if (connectTimeOut > 0) {
            clone.setConnectTimeout(connectTimeOut, TimeUnit.MINUTES);
        } else {
            clone.setConnectTimeout(DEFAULT_TIME_OUT, TimeUnit.MINUTES);
        }

        if (writeTimeOut > 0) {
            clone.setWriteTimeout(writeTimeOut, TimeUnit.MINUTES);
        } else {
            clone.setWriteTimeout(DEFAULT_TIME_OUT, TimeUnit.MINUTES);
        }

        if (readTimeOut > 0) {
            clone.setWriteTimeout(readTimeOut, TimeUnit.MINUTES);
        } else {
            clone.setWriteTimeout(DEFAULT_TIME_OUT, TimeUnit.MINUTES);
        }

        Call call = clone.newCall(request);
        call.enqueue(uploadListener);
        return call;
    }


    private static void addParams(MultipartBuilder builder, Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
                        RequestBody.create(null, params.get(key)));
            }
        }
    }

    private static void addFiles(MultipartBuilder builder, Pair<String, File>... files) {
        if (files != null) {
            RequestBody fileBody;
            for (int i = 0; i < files.length; i++) {
                Pair<String, File> filePair = files[i];
                String fileKeyName = filePair.first;
                File file = filePair.second;
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                builder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + fileKeyName + "\"; filename=\"" + fileName + "\""),
                        fileBody);
            }
        } else {
            throw new IllegalArgumentException("File can not be null");
        }
    }

    private static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    @Override
    Call execute(Callback callback) {
        return null;
    }
}
