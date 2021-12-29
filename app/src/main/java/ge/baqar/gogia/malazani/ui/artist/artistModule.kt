package ge.baqar.gogia.malazani.ui.artist

import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.dsl.module

@InternalCoroutinesApi
val artistModule = module {
    factory { ArtistViewModel(get(), get(), get(), get()) }
}

