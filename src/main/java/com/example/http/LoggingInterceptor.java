package com.example.http;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoggingInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String requestUrl = request.url().toString();
        String requestBody = null;
        if (request.body() != null) {
            requestBody = bodyToString(request.body());
        }

        logger.info("Request: {} | Headers: {} | Body: {}", requestUrl, request.headers(), requestBody);

        Response response = chain.proceed(request);
        String responseBody = null;
        if (response.body() != null) {
            responseBody = bodyToString(response.body());
        }

        logger.info("Response: {} | Code: {} | Headers: {} | Body: {}", requestUrl, response.code(), response.headers(), responseBody);

        return response;
    }

    private static String bodyToString(final RequestBody request) throws IOException {
        if (request == null) {
            return null;
        }
        final Request copy = request.newBuilder().build();
        final Buffer buffer = new Buffer();
        if (copy.method().equals("GET")) {
            logger.info(copy.url() + " " + copy.headers());
        } else {
            if (copy.body() != null) {
                copy.body().writeTo(buffer);
            }
        }
        return buffer.readUtf8();
    }

    private static String bodyToString(final ResponseBody responseBody) throws IOException {
        final Buffer buffer = new Buffer();
        if (responseBody != null) {
            responseBody.writeTo(buffer);
        }
        return buffer.readUtf8();
    }
}