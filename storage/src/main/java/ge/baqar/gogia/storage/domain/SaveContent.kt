package ge.baqar.gogia.storage.domain

import ge.baqar.gogia.storage.utils.DOT

interface SaveContent {
    val fileNameWithoutSuffix: String
    val suffix: String
    val mimeType: String?
    val subfolderName: String?

    val fileNameWithSuffix: String
        get() = "$fileNameWithoutSuffix$DOT${suffix}"

}