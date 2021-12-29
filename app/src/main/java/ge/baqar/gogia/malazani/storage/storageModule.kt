package ge.baqar.gogia.malazani.storage

import ge.baqar.gogia.db.FolkAppPreferences
import ge.baqar.gogia.db.provideFolkApiDatabase
import ge.baqar.gogia.storage.usecase.FileSaveController
import org.koin.dsl.module

val storageModule = module {
    single { FolkAppPreferences(get()) }
    single { AlbumDownloadProvider(get(), get(), get()) }
    single { FileSaveController.getInstance(get()) }
    factory { provideFolkApiDatabase(get()) }
}