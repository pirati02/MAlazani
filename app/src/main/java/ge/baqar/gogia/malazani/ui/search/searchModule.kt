package ge.baqar.gogia.malazani.ui.search

import org.koin.dsl.module

val searchModule = module {
    factory { SearchViewModel(get()) }
}

