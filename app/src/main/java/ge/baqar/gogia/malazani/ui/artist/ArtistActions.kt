import ge.baqar.gogia.malazani.poko.Ensemble

sealed class ArtistAction
class ArtistSongsRequested(val ensemble: Ensemble) : ArtistAction()
