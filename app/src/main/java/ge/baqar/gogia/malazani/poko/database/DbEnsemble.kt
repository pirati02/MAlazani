package ge.baqar.gogia.malazani.poko.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ge.baqar.gogia.malazani.poko.ArtistType
import org.jetbrains.annotations.NotNull

@Entity(tableName = "Ensemble")
data class DbEnsemble(
    @PrimaryKey  val id: String,
    @ColumnInfo(name = "reference_id") @NotNull val referenceId: String,
    val name: String,
    var artistType: ArtistType
)
