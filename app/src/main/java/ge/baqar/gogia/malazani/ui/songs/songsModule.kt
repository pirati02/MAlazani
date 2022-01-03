package ge.baqar.gogia.malazani.ui.songs

import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.dsl.module

@InternalCoroutinesApi
val songsModule = module {
    factory { SongsViewModel(get(), get(), get(), get()) }
}

