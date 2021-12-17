package ge.baqar.gogia.malazani.poko.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DbEnsemble(
    @PrimaryKey  val id: String,
    val name: String,
    var artistType: String
)
