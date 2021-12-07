package ge.baqar.gogia.malazani.utility

import ge.baqar.gogia.malazani.media.player.AudioPlayer
import ge.baqar.gogia.malazani.media.player.AudioPlayerImpl
import org.koin.dsl.module

val utilityModule = module {
    factory { NetworkStatus(get()) }
    single<AudioPlayer> { AudioPlayerImpl() }
}