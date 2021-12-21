package ge.baqar.gogia.malazani.ui.artists

import ge.baqar.gogia.malazani.poko.Ensemble

//Actions
open class ArtistsAction
data class ArtistsLoaded(val artists: MutableList<Ensemble>) : ArtistsAction()
class EnsemblesRequested : ArtistsAction()
class OldRecordingsRequested : ArtistsAction()