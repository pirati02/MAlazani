package ge.baqar.gogia.malazani.poko.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ge.baqar.gogia.malazani.poko.SongType

@Entity(tableName = "Song")
data class DbSong(
    @PrimaryKey val Id: String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "path") val path: String?,
    @ColumnInfo(name = "ensemble_id") val ensembleId: String?,
    @ColumnInfo(name = "song_type") val songType: SongType,
    @ColumnInfo(name = "data") var data: ByteArray?
)