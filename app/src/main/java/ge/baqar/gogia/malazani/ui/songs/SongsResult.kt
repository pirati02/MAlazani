package ge.baqar.gogia.malazani.ui.songs

import ge.baqar.gogia.model.Song

open class SongsResult

data class ArtistState(
    val isInProgress: Boolean,
    val chants: MutableList<Song>,
    val songs: MutableList<Song>,
    val error: String?
) : SongsResult() {


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