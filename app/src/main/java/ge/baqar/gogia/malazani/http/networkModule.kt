package ge.baqar.gogia.malazani.http

import ge.baqar.gogia.http.provideFolkApiEndpoint
import ge.baqar.gogia.http.provideOkHttpClient
import ge.baqar.gogia.http.provideRetrofit
import ge.baqar.gogia.http.provideSearchEndpoint
import org.koin.dsl.module


val networkModule = module {
    factory { provideFolkApiEndpoint() }
    factory { provideSearchEndpoint() }
    factory { provideOkHttpClient() }
    factory { provideRetrofit(get()) }
}