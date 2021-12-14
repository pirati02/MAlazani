import ge.baqar.gogia.malazani.poko.Ensemble

sealed class ArtistAction
class ArtistSongsRequested(val ensemble: Ensemble) : ArtistAction()
class ArtistChantsRequested  : ArtistAction() {
    var ensemble: Ensemble? = null
        set(value) {
            field = value?.apply {
                link = link.replace("xalxuri-simgerebi", "saeklesio-sagaloblebi")
            }
        }
}