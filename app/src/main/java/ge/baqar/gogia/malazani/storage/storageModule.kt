package ge.baqar.gogia.malazani.storage

import android.content.Context
import androidx.room.Room
import ge.baqar.gogia.malazani.storage.db.FolkApiDao
import ge.baqar.gogia.malazani.storage.db.FolkApiDatabase
import ge.baqar.gogia.storage.usecase.FileSaveController
import org.koin.dsl.module

val storageModule = module {
    single { FolkAppPreferences(get()) }
    single { AlbumDownloadProvider(get(), get(), get()) }
    single { FileSaveController.getInstance(get()) }
    factory { provideFolkApiDatabase(get()) }
}


fun provideFolkApiDatabase(context: Context): FolkApiDao? {
    val db =  Room.databaseBuilder(
        context,
        FolkApiDatabase::class.java,
        "folkapidb"
    ).build()

    return db.folkApiDao()
}