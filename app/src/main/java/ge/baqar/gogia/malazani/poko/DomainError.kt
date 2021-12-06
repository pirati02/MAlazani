package ge.baqar.gogia.malazani.poko

interface DomainError {
    val message: String?
    val exception: Exception?
}