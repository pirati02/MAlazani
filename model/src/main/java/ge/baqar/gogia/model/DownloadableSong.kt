package ge.baqar.gogia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class DownloadableSong(
    val id: String,
    val name: String,
    val nameEng: String,
    val link: String,
    val songType: SongType,
    val ensembleId: String
) : Parcelable {
}
