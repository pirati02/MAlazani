package ge.baqar.gogia.malazani.poko

data class Song(
    val id: String,
    val name: String,
    val path: String,
    val songType: SongType,
    val ensembleId: String,
    var isPlaying: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (other is Song) {
            return other.name == name
                    && other.path == path
                    && other.ensembleId == ensembleId
                    && other.songType == songType
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}

enum class SongType {
    Song,
    Chant
}