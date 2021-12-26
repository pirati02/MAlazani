package ge.baqar.gogia.malazani.ui.artist

import org.koin.dsl.module

val artistModule = module {
    factory { ArtistViewModel(get(), get(), get()) }
}

