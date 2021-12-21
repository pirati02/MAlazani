package ge.baqar.gogia.malazani.storage

import org.koin.dsl.module

val storageModule = module {
    single { FolkAppPreferences(get()) }
}