package com.example.module.http;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class HttpClientModule {
    private final Retrofit retrofit;

    public HttpClientModule() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        System.out.println("Request: " + request.toString());
                        long t1 = System.nanoTime();
                        Response response = chain.proceed(request);
                        long t2 = System.nanoTime();
                        System.out.printf("Response received in %.1fms%n", (t2 - t1) / 1e6d);
                        return response;
                    }
                })
                .build();

        this.retrofit = new Retrofit.Builder()
                .baseUrl("https://api.example.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public FolkApiService getFolkApiService() {
        return retrofit.create(FolkApiService.class);
    }

    public SearchService getSearchService() {
        return retrofit.create(SearchService.class);
    }
}