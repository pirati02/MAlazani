package ge.baqar.gogia.malazani

import android.app.Application
import ge.baqar.gogia.malazani.http.networkModule
import ge.baqar.gogia.malazani.media.mediaModule
import ge.baqar.gogia.malazani.storage.storageModule
import ge.baqar.gogia.malazani.ui.ensembles.ensemblesModule
import ge.baqar.gogia.malazani.ui.favourites.favouritesModule
import ge.baqar.gogia.malazani.ui.search.searchModule
import ge.baqar.gogia.malazani.ui.songs.songsModule
import ge.baqar.gogia.malazani.utility.utilityModule
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import kotlin.time.ExperimentalTime

@ExperimentalTime
@InternalCoroutinesApi
class FolkApplication : Application() {
    companion object {
        var instance: Application? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@FolkApplication)
            modules(
                mutableListOf(
                    utilityModule,
                    mediaModule,
                    networkModule,
                    storageModule,
                    ensemblesModule,
                    songsModule,
                    searchModule,
                    favouritesModule
                )
            )
        }
    }
}