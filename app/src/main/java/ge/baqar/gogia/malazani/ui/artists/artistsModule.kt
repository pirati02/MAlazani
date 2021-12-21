package ge.baqar.gogia.malazani.ui.artists

import ge.baqar.gogia.malazani.http.repository.FolkApiRepository
import org.koin.dsl.module

val artistsModule = module {

    single { FolkApiRepository(get(), get(), get()) }
    //viewModel
    factory { ArtistsViewModel(get()) }
}

