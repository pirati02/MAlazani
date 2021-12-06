package ge.baqar.gogia.malazani.http

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import org.koin.dsl.module

val networkModule = module {
    factory { provideVolleyQueue(get()) }
}

fun provideVolleyQueue(context: Context): RequestQueue {
    return Volley.newRequestQueue(context)
}

private val alazaniUrl: String = "http://www.alazani.ge"
//
//fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
//    return Retrofit.Builder()
//        .baseUrl(alazaniUrl)
//        .client(okHttpClient)
//        .addConverterFactory(ScalarsConverterFactory.create())
//        .build()
//}
//
//fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
//    return OkHttpClient().newBuilder().addInterceptor(authInterceptor).build()
//}
//
//fun providAlazaniApi(retrofit: Retrofit): AlazaniService =
//    retrofit.create(AlazaniService::class.java)
