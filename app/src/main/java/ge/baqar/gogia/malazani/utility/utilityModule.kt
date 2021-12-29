package ge.baqar.gogia.malazani.utility

import ge.baqar.gogia.utils.FileExtensions
import ge.baqar.gogia.utils.NetworkStatus
import org.koin.dsl.module

val utilityModule = module {
    factory { NetworkStatus(get()) }
    single { FileExtensions(get()) }
}