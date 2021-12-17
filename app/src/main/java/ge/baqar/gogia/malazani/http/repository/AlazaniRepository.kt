package ge.baqar.gogia.malazani.http.repository

import android.os.Build
import androidx.annotation.RequiresApi
import ge.baqar.gogia.malazani.arch.ReactiveResult
import ge.baqar.gogia.malazani.arch.asError
import ge.baqar.gogia.malazani.arch.asSuccess
import ge.baqar.gogia.malazani.http.FolkApiService
import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.SongsResponse
import ge.baqar.gogia.malazani.utility.NetworkStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*

class AlazaniRepository(
    private var networkStatus: NetworkStatus,
    private var folkApiService: FolkApiService
) {
    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun ensembles(): Flow<ReactiveResult<String, MutableList<Ensemble>>> {
        return coroutineScope {
            if (networkStatus.isOnline()) {
                val ensembles = folkApiService.ensembles()
                val flow = callbackFlow<ReactiveResult<String, MutableList<Ensemble>>> {
                    trySend(ensembles.asSuccess)
                    awaitClose { channel.close() }
                }
                return@coroutineScope flow
            } else {
                return@coroutineScope flowOf("network_is_off".asError)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun oldRecordings(): Flow<ReactiveResult<String, MutableList<Ensemble>>> {
        return coroutineScope {
            if (networkStatus.isOnline()) {
                val ensembles = folkApiService.oldRecordings()
                val flow = callbackFlow<ReactiveResult<String, MutableList<Ensemble>>> {
                    trySend(ensembles.asSuccess)
                    awaitClose { channel.close() }
                }
                return@coroutineScope flow
            } else {
                return@coroutineScope flowOf("network_is_off".asError)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun songs(id: String): Flow<ReactiveResult<String, SongsResponse>> {
        return coroutineScope {
            if (networkStatus.isOnline()) {
                val songs = folkApiService.songs(id)
                val flow = callbackFlow<ReactiveResult<String, SongsResponse>> {
                    trySend(songs.asSuccess)
                    awaitClose { channel.close() }
                }
                return@coroutineScope flow
            } else {
                return@coroutineScope flowOf("network_is_off".asError)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun downloadSong(path: String): Flow<ReactiveResult<String, ByteArray>> {
        return coroutineScope {
            if (networkStatus.isOnline()) {
                val song = folkApiService.downloadSongData(path).body()?.bytes()
                val flow = callbackFlow<ReactiveResult<String, ByteArray>> {
                    trySend(song?.asSuccess!!)
                    awaitClose { channel.close() }
                }
                return@coroutineScope flow
            } else {
                return@coroutineScope flowOf("network_is_off".asError)
            }
        }
    }
}