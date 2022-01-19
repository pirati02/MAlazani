package ge.baqar.gogia.db.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ge.baqar.gogia.db.model.DbEnsemble
import ge.baqar.gogia.db.model.DbSong

@Database(entities = [DbSong::class, DbEnsemble::class], version = 3)
abstract class FolkApiDatabase : RoomDatabase() {
    abstract fun folkApiDao(): FolkApiDao?
}