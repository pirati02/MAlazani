package ge.baqar.gogia.malazani

import android.app.Application
import ge.baqar.gogia.malazani.http.networkModule
import ge.baqar.gogia.malazani.media.mediaModule
import ge.baqar.gogia.malazani.storage.FolkApiDatabase
import ge.baqar.gogia.malazani.storage.storageModule
import ge.baqar.gogia.malazani.ui.artist.artistModule
import ge.baqar.gogia.malazani.ui.artists.artistsModule
import ge.baqar.gogia.malazani.utility.utilityModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

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
                    artistsModule,
                    artistModule
                )
            )
        }
    }
}