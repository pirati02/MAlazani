package ge.baqar.gogia.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ge.baqar.gogia.model.ArtistType
import org.jetbrains.annotations.NotNull

@Entity(tableName = "Ensemble")
data class DbEnsemble(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "reference_id") @NotNull val referenceId: String,
    val name: String,
    val nameEng: String,
    var artistType: ArtistType
)