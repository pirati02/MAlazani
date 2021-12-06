import ge.baqar.gogia.malazani.poko.AlazaniArtist

//Actions
sealed class ArtistsAction()
object EmptyAction : ArtistsAction()
data class ArtistsLoaded(val Artists: MutableList<AlazaniArtist>) : ArtistsAction()
class ArtistsRequested(val link: String) : ArtistsAction()