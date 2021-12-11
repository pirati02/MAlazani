package ge.baqar.gogia.malazani.poko

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class Song(val title: String, val link: String)
@Parcelize
data class Ensemble(val title: String, val link: String) : Parcelable