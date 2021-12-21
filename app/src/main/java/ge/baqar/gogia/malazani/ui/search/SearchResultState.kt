package ge.baqar.gogia.malazani.ui.search

import ge.baqar.gogia.malazani.poko.SearchResult

open class SearchResultState

data class SearchState(
    val isInProgress: Boolean,
    val result: SearchResult?,
    val error: String?
) : SearchResultState() {

    companion object {
        val DEFAULT = SearchState(
            isInProgress = false,
            error = null,
            result = null
        )

        val LOADING = SearchState(
            isInProgress = true,
            error = null,
            result = null
        )
    }
}
