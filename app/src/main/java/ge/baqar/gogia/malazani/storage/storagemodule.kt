package ge.baqar.gogia.malazani.storage

import android.content.Context
import androidx.room.Room
import org.koin.dsl.module

val storageModule = module {
    //factory { provideFolkApiDatabase(get()) }
}

fun provideFolkApiDatabase(context: Context): FolkApiDatabase {
    return Room.databaseBuilder(
        context,
        FolkApiDatabase::class.java, "folkapidb"
    ).build()
}