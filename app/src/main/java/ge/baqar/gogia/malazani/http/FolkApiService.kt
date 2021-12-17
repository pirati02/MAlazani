package ge.baqar.gogia.malazani.http

import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.SongsResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FolkApiService {
    @GET("ensembles")
    suspend fun ensembles(): MutableList<Ensemble>

    @GET("old-recordings")
    suspend fun oldRecordings(): MutableList<Ensemble>

    @GET("songs/{id}")
    suspend fun songs(@Path("id") id: String): SongsResponse

    @Streaming
    @GET
    suspend fun downloadSongData(@Url fileUrl:String): Response<ResponseBody>
}