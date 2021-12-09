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
