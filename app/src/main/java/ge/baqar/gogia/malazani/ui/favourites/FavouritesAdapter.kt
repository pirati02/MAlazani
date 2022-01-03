package ge.baqar.gogia.malazani.ui.favourites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.model.Song

class FavouritesAdapter(
    var dataSource: MutableList<Song>,
    private val callback: (Int, Song) -> Unit) : RecyclerView.Adapter<FavouritesAdapter.FavouritesViewHolder>() {

    inner class FavouritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songName: AppCompatTextView by lazy {
            itemView.findViewById(R.id.songNameText)
        }

        fun bind(position: Int, song: Song) {
            songName.text = "${song.name} - ${song.ensembleName}"
            itemView.setOnClickListener {
                callback(position, song)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavouritesAdapter.FavouritesViewHolder {
        return FavouritesViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_favourite_result_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FavouritesViewHolder, position: Int) {
        holder.bind(position, dataSource[position])
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }
}