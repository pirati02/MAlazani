package ge.baqar.gogia.malazani.ui.artists

import ge.baqar.gogia.malazani.http.repository.AlazaniRepository
import org.koin.dsl.module

val artistsModule = module {

    single { AlazaniRepository(get(), get()) }
    //viewModel
    factory { ArtistsViewModel(get()) }
}

