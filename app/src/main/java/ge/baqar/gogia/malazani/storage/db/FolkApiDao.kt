package ge.baqar.gogia.malazani.storage.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ge.baqar.gogia.malazani.poko.db.DbEnsemble
import ge.baqar.gogia.malazani.poko.db.DbSong

@Dao
interface FolkApiDao {

    @Query("SELECT * FROM Song WHERE ensemble_id = :ensembleId")
    fun songsByEnsembleId(ensembleId: String): MutableList<DbSong>

    @Query("SELECT * FROM Ensemble WHERE reference_id = :id")
    fun ensembleById(id: String): DbEnsemble

    @Query("SELECT * FROM Ensemble")
    fun ensembles(): MutableList<DbEnsemble>

    @Insert
    suspend fun saveSong(song: DbSong)

    @Insert
    suspend fun saveEnsemble(ensemble: DbEnsemble?)

    @Query("DELETE FROM Song WHERE ensemble_id = :ensembleId")
    suspend fun removeSongsByEnsembleId(ensembleId: String)

    @Query("DELETE FROM Ensemble WHERE reference_id = :ensembleId")
    fun removeEnsemble(ensembleId: String)

    @Query("DELETE FROM Song WHERE id in (:ids)")
    fun removeSongsByIds(ids: List<String>)
}