package ge.baqar.gogia.malazani.poko

data class ConnectionError(
    override val message: String,
    override val exception: Exception
) : DomainError