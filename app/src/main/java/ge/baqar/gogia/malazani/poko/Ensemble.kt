package ge.baqar.gogia.malazani.poko

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class Ensemble(
    override val id: String,
    val name: String,
    var nameEng: String,
    var artistType: ArtistType,
    var isPlaying: Boolean = false
) : SearchedItem, Parcelable {
    override fun detailedName(): String {
        return name
    }
}

enum class ArtistType(val type: String) {
    @SerializedName("1")
    ENSEMBLE("1"),
    @SerializedName("2")
    OLD_RECORDING("2")
}