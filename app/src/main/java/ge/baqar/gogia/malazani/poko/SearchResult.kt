package ge.baqar.gogia.malazani.poko

data class SearchResult(val ensembles: MutableList<Ensemble>, val songs: MutableList<Song>)
interface SearchedItem {
    val id: String
    fun detailedName(): String
}