package ge.baqar.gogia.malazani.storage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ge.baqar.gogia.malazani.poko.Song
import ge.baqar.gogia.malazani.poko.database.DbEnsemble
import ge.baqar.gogia.malazani.poko.database.DbSong

@Dao
interface FolkApiDao {

    @Query("SELECT * FROM Song WHERE ensemble_id = :ensembleId")
    suspend fun songsByEnsembleId(ensembleId: IntArray): List<DbSong>

    @Insert
    suspend fun saveSongs(songs: MutableList<DbSong>?)

    @Insert
    suspend fun saveEnsemble(ensemble: DbEnsemble?)

    @Delete
    suspend fun removeSongs(songs: MutableList<Song>)
}