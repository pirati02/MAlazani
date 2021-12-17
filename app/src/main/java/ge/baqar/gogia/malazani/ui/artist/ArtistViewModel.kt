package ge.baqar.gogia.malazani.ui.artist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import ge.baqar.gogia.malazani.arch.FailedResult
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.AlazaniRepository
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.Song
import ge.baqar.gogia.malazani.poko.StorageOption
import ge.baqar.gogia.malazani.poko.database.DbEnsemble
import ge.baqar.gogia.malazani.poko.database.DbSong
import ge.baqar.gogia.malazani.storage.FolkApiDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ArtistViewModel(
    private val alazaniRepository: AlazaniRepository,
    private val folkApiDao: FolkApiDao?
) : ReactiveViewModel<ArtistAction, ArtistResult, ArtistState>(ArtistState.DEFAULT) {

    @RequiresApi(Build.VERSION_CODES.M)
    fun songs(
        ensemble: Ensemble
    ) = update {
        emit {
            ArtistState.IS_LOADING
        }

        alazaniRepository.songs(ensemble.id).collect { result ->
            if (result is SucceedResult) {
                emit {
                    SongsState.dataLoaded(result.value.songs)
                }

                emit {
                    ChantsState.dataLoaded(result.value.chants)
                }
            }
            if (result is FailedResult) {
                emit {
                    ChantsState.error(result.value)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun saveArtistAndAlbum(ensemble: Ensemble, songs: MutableList<Song>?) = update {
        viewModelScope.launch(Dispatchers.IO) {
            val convertedSongs = songs
                ?.map { DbSong(it.id, it.name, it.path, it.ensembleId, it.songType, null) }
                ?.toMutableList()
            convertedSongs?.forEach { dbSong ->
                alazaniRepository.downloadSong(dbSong.path!!).collect {
                    if (it is SucceedResult) {
                        dbSong.data = it.value
                    }
                }
            }

            val convertedEnsemble = DbEnsemble(ensemble.id, ensemble.name, ensemble.artistType.toString())
            folkApiDao?.saveEnsemble(convertedEnsemble)
            folkApiDao?.saveSongs(convertedSongs)
        }
    }

    private fun downloadExternal(ensemble: Ensemble, songs: MutableList<Song>?) = update {
        emit {
            DownloadExternalState(songs, ensemble)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun ArtistAction.process(): Flow<() -> ArtistResult> {
        return when (this) {
            is ArtistSongsRequested -> {
                songs(ensemble)
            }
            is ArtistSongsDownloadRequested -> {
                if (storageOption == StorageOption.ApplicationCache)
                    saveArtistAndAlbum(ensemble, songs)
                else
                    downloadExternal(ensemble, songs)
            }
            else -> update {

            }
        }
    }
}