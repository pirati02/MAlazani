package ge.baqar.gogia.malazani.ui.artist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.poko.Song

class SongsAdapter(
    val dataSource: MutableList<Song>,
    val clicked: (Song, Int) -> Unit
) : RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: AppCompatTextView by lazy {
            itemView.findViewById(R.id.artistTitle)
        }

        fun bind(artist: Song, position: Int) {
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