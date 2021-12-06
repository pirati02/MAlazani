package ge.baqar.gogia.malazani.ui.artists

import ge.baqar.gogia.malazani.http.repository.AlazaniRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi

import org.koin.dsl.module

@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
val artistsModule = module {

    single { AlazaniRepositoryImpl(get(), get()) }
    //viewModel
    factory { ArtistsViewModel(get()) }
}

