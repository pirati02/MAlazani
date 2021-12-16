package ge.baqar.gogia.malazani.http

import ge.baqar.gogia.malazani.poko.Ensemble
import ge.baqar.gogia.malazani.poko.SongsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface FolkApiService {
    @GET("ensembles")
    suspend fun ensembles(): MutableList<Ensemble>

    @GET("old-recordings")
    suspend fun oldRecordings(): MutableList<Ensemble>

    @GET("songs/{id}")
    suspend fun songs(@Path("id") id: String): SongsResponse
}