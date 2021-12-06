package ge.baqar.gogia.malazani.ui.artists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.poko.AlazaniArtistListItem

class ArtistsAdapter(
    private val dataSource: MutableList<AlazaniArtistListItem>,
    val clicked: (AlazaniArtistListItem) -> Unit
) : RecyclerView.Adapter<ArtistsAdapter.EnsembleViewHolder>() {
    inner class EnsembleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: AppCompatTextView by lazy {
            itemView.findViewById(R.id.artistTitle)
        }
        var position: Int? = null

        fun bind(artist: AlazaniArtistListItem) {
            name.text = artist.title

            itemView.setOnClickListener {
                clicked.invoke(artist)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnsembleViewHolder {
        return EnsembleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_alazani_artist, parent, false)
        )
    }

    override fun onBindViewHolder(holder: EnsembleViewHolder, position: Int) {
        holder.bind(dataSource[position])
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }
}