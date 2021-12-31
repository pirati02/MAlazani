package ge.baqar.gogia.model

import com.google.gson.annotations.SerializedName

data class Song(
    override val id: String,
    val name: String,
    var nameEng: String,
    val path: String,
    val songType: SongType,
    val ensembleId: String,
    val ensembleName: String,
    var isPlaying: Boolean = false,
    val data: ByteArray? = null,
    var availableOffline: Boolean = false
) : SearchedItem {


    override fun equals(other: Any?): Boolean {
        if (other is Song) {
            return other.name == name
                    && other.path == path
                    && other.ensembleId == ensembleId
                    && other.songType == songType
        }
        return super.equals(other)
    }

    override fun detailedName(): String {
        return "$name - $ensembleName"
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}

enum class SongType(val index: Int) {
    @SerializedName("0")
    Song(0),

    @SerializedName("1")
    Chant(1)
}