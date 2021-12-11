package ge.baqar.gogia.malazani.ui.artist

import ge.baqar.gogia.malazani.poko.Song

open class ArtistResult

open class ArtistState(
    open val isInProgress: Boolean,
    open val error: String?
) : ArtistResult() {


    companion object {
        val DEFAULT = ArtistState(
            isInProgress = false,
            error = null
        )
        val IS_LOADING = ArtistState(
            isInProgress = true,
            error = null
        )
    }
}

data class ChantsState(
    override val isInProgress: Boolean,
    val chants: MutableList<Song>,
    override val error: String?
) : ArtistState(isInProgress, error) {
    companion object {
        fun dataLoaded(chants: MutableList<Song>): ArtistState {
            return ChantsState(isInProgress = false, chants, null)
        }

        fun error(message: String?): ChantsState {
            return ChantsState(isInProgress = false, mutableListOf(), message)
        }
    }
}

data class SongsState(
    override val isInProgress: Boolean,
    val songs: MutableList<Song>,
    override val error: String?
) : ArtistState(isInProgress, error) {
    companion object {
        fun dataLoaded(songs: MutableList<Song>): SongsState {
            return SongsState(isInProgress = false, songs, null)
        }

        fun error(message: String?): SongsState {
            return SongsState(isInProgress = false, mutableListOf(), message)
        }
    }
}