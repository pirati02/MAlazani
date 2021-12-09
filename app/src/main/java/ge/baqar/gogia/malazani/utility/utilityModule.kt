package ge.baqar.gogia.malazani.utility

import org.koin.dsl.module

val utilityModule = module {
    factory { NetworkStatus(get()) }
}