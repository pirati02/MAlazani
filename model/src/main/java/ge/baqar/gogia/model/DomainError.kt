package ge.baqar.gogia.model

interface DomainError {
    val message: String?
    val exception: Exception?
}