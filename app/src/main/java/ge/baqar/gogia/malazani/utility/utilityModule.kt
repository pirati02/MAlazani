package ge.baqar.gogia.malazani.utility

import ge.baqar.gogia.malazani.utility.NetworkStatus
import ge.baqar.gogia.malazani.utility.player.AudioPlayer
import ge.baqar.gogia.malazani.utility.player.AudioPlayerImpl
import org.koin.dsl.module
import kotlin.math.sin

val utilityModule = module {
    factory { NetworkStatus(get()) }
    single<AudioPlayer> { AudioPlayerImpl() }
}