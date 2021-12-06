package ge.baqar.gogia.malazani.utility.player

interface AudioPlayer {
    fun listenPlayer(callback: (Boolean) -> Unit)
    fun isPlaying(): Boolean
    fun release()
    fun play(audioData: String)
    fun pause()
    fun resume()
}