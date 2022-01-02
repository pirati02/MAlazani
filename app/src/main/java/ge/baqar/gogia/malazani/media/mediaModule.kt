package ge.baqar.gogia.malazani.media

import android.os.Build
import androidx.annotation.RequiresApi
import ge.baqar.gogia.malazani.media.player.AudioPlayer
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

@ExperimentalTime
@InternalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.O)
val mediaModule = module {
    single { AudioPlayer(get()) }
    single { MediaPlayerController(get(), get(), get(),  get()) }
}

