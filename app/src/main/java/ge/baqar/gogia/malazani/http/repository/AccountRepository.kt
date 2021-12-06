package ge.baqar.gogia.malazani.http.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import ge.baqar.gogia.malazani.arch.ReactiveResult
import ge.baqar.gogia.malazani.arch.asError
import ge.baqar.gogia.malazani.arch.asSuccess
import ge.baqar.gogia.malazani.poko.ConnectionError
import ge.baqar.gogia.malazani.utility.NetworkStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import java.io.IOException


@ExperimentalCoroutinesApi
class AlazaniRepositoryImpl(
    private var networkStatus: NetworkStatus,
    private var queue: RequestQueue
) {
    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun artists(link: String): Flow<ReactiveResult<VolleyError, String>> {
        return coroutineScope {
            if (networkStatus.isOnline()) {
                val flow = callbackFlow< ReactiveResult<VolleyError, String> >{
                    val stringRequest = StringRequest(Request.Method.GET, link,
                        { response ->
                            trySend(response.asSuccess)
                        }, { trySend(it.asError) })

                    queue.add(stringRequest)
                    awaitClose { channel.close() }
                }
                return@coroutineScope flow
            } else {
                return@coroutineScope flowOf(VolleyError("Local storage not implemented").asError)
            }
        }
    }
}