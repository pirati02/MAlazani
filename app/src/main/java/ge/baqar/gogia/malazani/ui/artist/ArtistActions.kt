package ge.baqar.gogia.malazani.ui.artist

import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.Song
import ge.baqar.gogia.malazani.poko.StorageOption

open class ArtistAction()
class ArtistSongsRequested(val ensemble: Ensemble) : ArtistAction()
class ArtistSongsDownloadRequested(
    val ensemble: Ensemble,
    val songs: MutableList<Song>?,
    val storageOption: StorageOption
) :
    ArtistAction()
