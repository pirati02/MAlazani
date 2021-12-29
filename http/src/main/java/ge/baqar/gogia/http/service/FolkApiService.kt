package ge.baqar.gogia.http.service

import ge.baqar.gogia.model.Ensemble
import ge.baqar.gogia.model.SongsResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FolkApiService {
    @GET("folkapi/ensembles")
    suspend fun ensembles(): MutableList<Ensemble>

    @GET("folkapi/ensemble/{id}")
    suspend fun ensemble(@Path("id") id: String): Ensemble

    @GET("folkapi/old-recordings")
    suspend fun oldRecordings(): MutableList<Ensemble>

    @GET("folkapi/songs/{id}")
    suspend fun songs(@Path("id") id: String): SongsResponse

    @Streaming
    @GET
    suspend fun downloadSongData(@Url fileUrl:String): Response<ResponseBody>
}