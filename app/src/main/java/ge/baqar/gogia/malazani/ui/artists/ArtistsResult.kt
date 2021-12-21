package ge.baqar.gogia.malazani.ui.artists

import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.Song

open class ArtistsResult

data class ArtistsState(
    val isInProgress: Boolean,
    val artists: MutableList<Ensemble>,
    val error: String?
) : ArtistsResult() {

    companion object {
        val DEFAULT = ArtistsState(
            isInProgress = false,
            error = null,
            artists = mutableListOf()
        )
    }
}
