package ge.baqar.gogia.malazani.ui.artist

import ge.baqar.gogia.malazani.storage.AlbumDownloadProvider
import org.koin.dsl.module

val artistModule = module {
    single { AlbumDownloadProvider(get(), get()) }
    factory { ArtistViewModel(get(), get()) }
}

