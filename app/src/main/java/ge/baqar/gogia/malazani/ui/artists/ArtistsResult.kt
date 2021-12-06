package ge.baqar.gogia.malazani.ui.artists

import ge.baqar.gogia.malazani.poko.AlazaniArtistListItem

open class ArtistsResult

data class ArtistsState(
    val isInProgress: Boolean,
    val Artists: MutableList<AlazaniArtistListItem>,
    val error: String?
) : ArtistsResult() {

    companion object {
        val DEFAULT = ArtistsState(
            isInProgress = false,
            error = null,
            Artists = mutableListOf(),
//            newMessage =  null
        )
    }
}
