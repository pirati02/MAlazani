package ge.baqar.gogia.malazani.storage

import android.content.Context
import androidx.room.Room
import ge.baqar.gogia.malazani.storage.prefs.FolkAppPreferences
import org.koin.dsl.module

val storageModule = module {
    single { FolkAppPreferences(get()) }
    factory { provideFolkApiDatabase(get()) }
}


fun provideFolkApiDatabase(context: Context): FolkApiDao? {
    val db =  Room.databaseBuilder(
        context,
        FolkApiDatabase::class.java, "folkapidb"
    ).build()

    return db.folkApiDao()
}