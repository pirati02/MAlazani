import ge.baqar.gogia.malazani.poko.AlazaniArtist

//Actions
sealed class ArtistAction()
class ArtistSongsRequested(val link: String) : ArtistAction()
class ArtistChantsRequested : ArtistAction() {
    var link: String? = null
        set(value) {
            field = value?.replace("xalxuri-simgerebi", "saeklesio-sagaloblebi")
        }
}