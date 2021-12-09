package ge.baqar.gogia.malazani.media.player

interface AudioPlayer {
    fun listenPlayer(callback: (Boolean) -> Unit)
    fun isPlaying(): Boolean
    fun release()
    fun play(audioData: String, callback: () -> Unit)
    fun pause()
    fun resume()
    fun updateTimeHandler(callback: (Long?, String?) -> Unit)
    fun completed(callback: () -> Unit)
    fun getDurationString(): String
    fun getDuration(): Long
    fun playOn(progress: Int?)
}