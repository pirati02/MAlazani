package ge.baqar.gogia.malazani

import android.app.Application
import ge.baqar.gogia.malazani.http.networkModule
import ge.baqar.gogia.malazani.media.mediaModule
import ge.baqar.gogia.malazani.ui.artists.artistsModule
import ge.baqar.gogia.malazani.ui.artist.artistModule
import ge.baqar.gogia.malazani.utility.utilityModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
class AlazaniApp : Application() {
    companion object {
        var instance: Application? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AlazaniApp)
            modules(
                mutableListOf(
                    utilityModule,
                    mediaModule,
                    networkModule,
                    artistsModule,
                    artistModule
                )
            )
        }
    }
}