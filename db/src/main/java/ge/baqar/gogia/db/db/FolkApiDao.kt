package ge.baqar.gogia.db.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ge.baqar.gogia.db.model.DbEnsemble
import ge.baqar.gogia.db.model.DbSong

@Dao
interface FolkApiDao {


    @Insert
    suspend fun saveSong(song: DbSong)

    @Insert
    suspend fun saveEnsemble(ensemble: DbEnsemble?)

    @Insert
    suspend fun saveCurrentSong(song: DbSong)

    @Insert
    fun saveCurrentEnsemble(ensemble: DbEnsemble)

    @Query("DELETE FROM Song WHERE ensemble_id = :ensembleId")
    suspend fun removeSongsByEnsembleId(ensembleId: String)

    @Query("DELETE FROM Ensemble WHERE reference_id = :ensembleId")
    fun removeEnsemble(ensembleId: String)

    @Query("DELETE FROM Song WHERE reference_id in (:ids)")
    suspend fun removeSongsByIds(ids: List<String>)

    @Query("SELECT * FROM Song WHERE reference_id = :songId")
    suspend fun song(songId: String): DbSong?

    @Query("UPDATE Song SET is_current = 1")
    suspend fun updateAllSongAsNoCurrent()

    @Query("SELECT * FROM Song WHERE is_current = 1")
    suspend fun getCurrentSong(): DbSong?

    @Query("UPDATE Ensemble SET is_current = 1")
    fun updateAllEnsembleAsNoCurrent()

    @Query("SELECT * FROM Song ORDER By name_eng ASC")
    suspend fun songs(): MutableList<DbSong>

    @Query("SELECT * FROM Song WHERE ensemble_id = :ensembleId")
    fun songsByEnsembleId(ensembleId: String): MutableList<DbSong>

    @Query("SELECT * FROM Ensemble WHERE reference_id = :id")
    fun ensembleById(id: String): DbEnsemble?

    @Query("SELECT * FROM Ensemble")
    fun ensembles(): MutableList<DbEnsemble>

    @Query("SELECT * FROM Ensemble WHERE is_current = 1")
    suspend fun getCurrentEnsemble(): DbEnsemble?
}