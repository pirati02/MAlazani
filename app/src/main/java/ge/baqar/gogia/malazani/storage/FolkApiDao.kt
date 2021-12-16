package ge.baqar.gogia.malazani.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ge.baqar.gogia.malazani.poko.database.Song

@Dao
interface FolkApiDao {

    @Query("SELECT * FROM Song WHERE ensemble_id = :ensembleId")
    fun sonbsByEnsembleId(ensembleId: IntArray): List<Song>

    @Insert
    fun insertSongs(vararg users: Song)
}