package ge.baqar.gogia.malazani.poko

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Ensemble(val title: String, var link: String, var isPlaying: Boolean = false) : Parcelable