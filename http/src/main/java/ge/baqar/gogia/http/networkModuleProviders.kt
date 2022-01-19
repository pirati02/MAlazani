package ge.baqar.gogia.http

import ge.baqar.gogia.http.service.FolkApiService
import ge.baqar.gogia.http.service.SearchService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private const val baseURL = "https://rocky-cliffs-16276.herokuapp.com/"

fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(baseURL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient().newBuilder().build()
}


fun provideFolkApiEndpoint(): FolkApiService {
    return provideRetrofit(provideOkHttpClient()).create(FolkApiService::class.java)
}

fun provideSearchEndpoint(): SearchService {
    return provideRetrofit(provideOkHttpClient()).create(SearchService::class.java)
}