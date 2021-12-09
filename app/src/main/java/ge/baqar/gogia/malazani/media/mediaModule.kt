package ge.baqar.gogia.malazani.media

import ge.baqar.gogia.malazani.media.player.AudioPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.dsl.module

@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
val mediaModule = module {
    single { AudioPlayer(get()) }
    single { MediaPlayerController(get(), get()) }
}

