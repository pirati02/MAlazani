import ge.baqar.gogia.malazani.poko.Ensemble

//Actions
sealed class ArtistsAction()
object EmptyAction : ArtistsAction()
data class ArtistsLoaded(val Artists: MutableList<Ensemble>) : ArtistsAction()
class ArtistsRequested(val link: String) : ArtistsAction()