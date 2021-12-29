package ge.baqar.gogia.model

data class ConnectionError(
    override val message: String,
    override val exception: Exception
) : DomainError