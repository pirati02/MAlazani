package ge.baqar.gogia.malazani.ui.artist

import org.koin.dsl.module

val artistModule = module {
    //viewModel
    factory { ArtistViewModel(get(), get()) }
}

