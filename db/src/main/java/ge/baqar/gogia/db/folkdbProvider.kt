package ge.baqar.gogia.db

import android.content.Context
import androidx.room.Room
import ge.baqar.gogia.db.db.FolkApiDao
import ge.baqar.gogia.db.db.FolkApiDatabase


fun provideFolkApiDatabase(context: Context): FolkApiDao? {
    val db =  Room.databaseBuilder(
        context,
        FolkApiDatabase::class.java,
        "folkapidb"
    ).fallbackToDestructiveMigration()
        .build()

    return db.folkApiDao()
}