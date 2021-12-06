package ge.baqar.gogia.malazani.ui.artist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.poko.AlazaniArtistListItem

class SongsAdapter(
    private val dataSource: MutableList<AlazaniArtistListItem>,
    val clicked: (AlazaniArtistListItem, Int) -> Unit
) : RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: AppCompatTextView by lazy {
            itemView.findViewById(R.id.artistTitle)
        }
        fun bind(artist: AlazaniArtistListItem, position: Int) {
            name.text = artist.title

            itemView.setOnClickListener {
                clicked.invoke(artist, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_alazani_artist, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(dataSource[position], position)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }
}