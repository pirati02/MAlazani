package ge.baqar.gogia.malazani.ui.search

import androidx.lifecycle.viewModelScope
import ge.baqar.gogia.malazani.arch.FailedResult
import ge.baqar.gogia.malazani.arch.ReactiveViewModel
import ge.baqar.gogia.malazani.arch.SucceedResult
import ge.baqar.gogia.malazani.http.repository.FolkApiRepository
import ge.baqar.gogia.malazani.poko.Ensemble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchViewModel(
    val alazaniRepository: FolkApiRepository
) : ReactiveViewModel<SearchAction, SearchResultState, SearchState>(SearchState.DEFAULT) {
    override fun SearchAction.process(): Flow<() -> SearchResultState> {
        return when (this) {
            is DoSearch -> {
                doSearch(term)
            }
            is ClearSearchResult -> update {
                emit {
                    state.copy(isInProgress = false, error = null, result = null)
                }
            }
            else -> update {

            }
        }
    }

    private fun doSearch(term: String) = update {
        emit {
            SearchState.LOADING
        }

        alazaniRepository.search(term).collect { result ->
            if (result is SucceedResult) {
                emit {
                    state.copy(isInProgress = false, result = result.value)
                }
            }
            if (result is FailedResult) {
                emit { state.copy(isInProgress = false, error = result.value) }
            }
        }
    }

    fun ensembleById(ensembleId: String, completion: (Ensemble?) -> Unit){
        viewModelScope.launch {
            val ensemble = alazaniRepository.ensemble(ensembleId)
            completion(ensemble)
        }
    }
}