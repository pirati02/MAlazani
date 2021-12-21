package ge.baqar.gogia.malazani.ui.search

import ge.baqar.gogia.malazani.poko.SearchResult

//Actions
open class SearchAction

open class DoSearch(val term: String) : SearchAction()
object ClearSearchResult: SearchAction()
data class DataSearched(val result: SearchResult?) : SearchAction()