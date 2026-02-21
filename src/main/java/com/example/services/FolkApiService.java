package com.example.services;

import com.example.http.FolkApiClient;
import com.example.model.FolkApiResponse;
import okhttp3.Interceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FolkApiService {
    private final FolkApiClient folkApiClient;

    public FolkApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.folk.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        .addInterceptor(new LoggingInterceptor()) // Add logging interceptor
                        .build())
                .build();

        folkApiClient = retrofit.create(FolkApiClient.class);
    }

    public FolkApiResponse getFolkData() {
        return folkApiClient.getFolkData();
    }
}