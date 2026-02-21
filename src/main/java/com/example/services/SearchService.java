package com.example.services;

import com.example.http.SearchApiClient;
import com.example.model.SearchResponse;
import okhttp3.Interceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchService {
    private final SearchApiClient searchApiClient;

    public SearchService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.search.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        .addInterceptor(new LoggingInterceptor()) // Add logging interceptor
                        .build())
                .build();

        searchApiClient = retrofit.create(SearchApiClient.class);
    }

    public SearchResponse getSearchResults() {
        return searchApiClient.getSearchResults();
    }
}