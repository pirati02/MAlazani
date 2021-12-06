package ge.baqar.gogia.malazani.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
abstract class ReactiveViewModel<INPUT : Any, OUTPUT : Any, STATE : OUTPUT>(
    initialState: STATE
) : ViewModel() {

    private var mutableState = initialState
    val state get() = mutableState

    private var outputs: Flow<OUTPUT> = emptyFlow()

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Suppress("UNCHECKED_CAST")
    open fun intents(inputs: Flow<INPUT>): Flow<OUTPUT> {
        outputs = inputs
            .flowOn(Dispatchers.Main)
            .flatMapMerge { input -> input.process() }
            .map { transform -> transform() }
            .onEach { if (mutableState::class.isInstance(it)) mutableState = it as STATE }
            .onStart { emit(mutableState) }
            .flowOn(Dispatchers.Default)
            .shareIn(viewModelScope, SharingStarted.Lazily, 0)

        return outputs
    }

    protected fun update(
        block: suspend FlowCollector<() -> OUTPUT>.() -> Unit
    ): Flow<() -> OUTPUT> = flow(block)

    protected abstract fun INPUT.process(): Flow<() -> OUTPUT>
}
