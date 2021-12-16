package ge.baqar.gogia.malazani.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import ge.baqar.gogia.malazani.poko.database.Song

@Database(entities = [Song::class], version = 1)
abstract class FolkApiDatabase : RoomDatabase() {
    abstract fun folkApiDao(): FolkApiDao
}