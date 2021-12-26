package ge.baqar.gogia.malazani.ui.artist

import ge.baqar.gogia.malazani.poko.Song

open class ArtistResult

data class ArtistState(
    val isInProgress: Boolean,
    val chants: MutableList<Song>,
    val songs: MutableList<Song>,
    val error: String?
) : ArtistResult() {


    companion object {
        val DEFAULT = ArtistState(
            isInProgress = false,
            error = null,
            songs = mutableListOf(),
            chants = mutableListOf()
        )
        val IS_LOADING = ArtistState(
            isInProgress = true,
            error = null,
            songs = mutableListOf(),
            chants = mutableListOf()
        )
    }
}