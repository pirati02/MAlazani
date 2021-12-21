package ge.baqar.gogia.malazani.poko.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ge.baqar.gogia.malazani.poko.SongType
import org.jetbrains.annotations.NotNull

@Entity(tableName = "Song")
data class DbSong(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "reference_id") @NotNull val referenceId: String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "path") val path: String?,
    @ColumnInfo(name = "ensemble_id") val ensembleId: String?,
    @ColumnInfo(name = "song_type") val songType: SongType,
    @ColumnInfo(name = "data") var data: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (other is DbSong) {
            return other.referenceId == referenceId
                    && other.name == name
                    && other.ensembleId == ensembleId
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}