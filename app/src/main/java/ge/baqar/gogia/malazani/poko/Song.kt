package ge.baqar.gogia.malazani.poko

data class Song(
    val title: String,
    val link: String,
    val songType: SongType,
    val artistId: String,
    var isPlaying: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (other is Song) {
            return other.title == title
                    && other.link == link
                    && other.artistId == artistId
                    && other.songType == songType
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return link.hashCode()
    }
}

enum class SongType {
    Song,
    Chant
}