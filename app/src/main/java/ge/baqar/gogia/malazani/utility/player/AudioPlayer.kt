package ge.baqar.gogia.malazani.utility.player

interface AudioPlayer {
    suspend fun listenPlayer(callback: (Boolean) -> Unit)
    suspend fun isPlaying(): Boolean
    suspend fun release()
    suspend fun play(audioData: String)
    suspend fun pause()
    suspend fun resume()
    fun updateTimeHandler(callback: (Long?, String?) -> Unit)
    fun completed(callback: () -> Unit)
    fun getDurationString(): String
    fun getDuration(): Long
    fun playOn(progress: Int?)
}