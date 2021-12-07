package ge.baqar.gogia.malazani.ui.artist

import ge.baqar.gogia.malazani.media.MediaPlayerController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.dsl.module

@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
val artistModule = module {
    //viewModel
    factory { ArtistViewModel(get()) }
    factory { MediaPlayerController(get(), get()) }
}

