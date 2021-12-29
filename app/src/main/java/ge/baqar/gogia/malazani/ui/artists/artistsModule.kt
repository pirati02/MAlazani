package ge.baqar.gogia.malazani.ui.artists

import ge.baqar.gogia.http.repository.FolkApiRepository
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.dsl.module

@InternalCoroutinesApi
val artistsModule = module {

    single { FolkApiRepository(get(), get(), get()) }
    //viewModel
    factory { ArtistsViewModel(get(), get()) }
}