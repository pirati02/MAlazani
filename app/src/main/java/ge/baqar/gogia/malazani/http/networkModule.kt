package ge.baqar.gogia.malazani.http

import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    factory { provideFolkApiEndpoint() }
    factory { provideOkHttpClient() }
    factory { provideRetrofit(get()) }
}

private val baseURL = "https://rocky-cliffs-16276.herokuapp.com/folkapi/"

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