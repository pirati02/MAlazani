package ge.baqar.gogia.malazani.http.service

import ge.baqar.gogia.malazani.poko.SearchResult
import retrofit2.http.GET
import retrofit2.http.Path

interface SearchService {
    @GET("search/term/{term}")
    suspend fun search(@Path("term") term: String): SearchResult
}