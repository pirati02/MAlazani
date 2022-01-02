package ge.baqar.gogia.malazani.ui.favourites

import ge.baqar.gogia.model.Song

open class FavouriteResultState

data class FavouriteState(
    val isInProgress: Boolean,
    val favSongs: MutableList<Song>,
    val error: String?
) : FavouriteResultState() {

    companion object {
        val DEFAULT = FavouriteState(
            isInProgress = false,
            error = null,
            favSongs = mutableListOf()
        )

        val LOADING = FavouriteState(
            isInProgress = true,
            error = null,
            favSongs = mutableListOf()
        )
    }
}
