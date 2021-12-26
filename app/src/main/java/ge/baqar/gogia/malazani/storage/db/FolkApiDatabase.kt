package ge.baqar.gogia.malazani.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ge.baqar.gogia.malazani.poko.db.DbEnsemble
import ge.baqar.gogia.malazani.poko.db.DbSong

@Database(entities = [DbSong::class, DbEnsemble::class], version = 1)
abstract class FolkApiDatabase : RoomDatabase() {
    abstract fun folkApiDao(): FolkApiDao?
}