package ge.baqar.gogia.malazani.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import ge.baqar.gogia.malazani.R
import ge.baqar.gogia.malazani.poko.SearchedItem

class SearchedDataAdapter<Item: SearchedItem>(
    private val dataSource: MutableList<Item>,
    private val clicked: SearchedDataAdapter<Item>.(Item) -> Unit
) :
    RecyclerView.Adapter<SearchedDataAdapter<Item>.SearchedItemViewHolder>() {
    inner class SearchedItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: AppCompatTextView by lazy {
            itemView.findViewById(R.id.title)
        }

        fun bind(item: Item) {
            name.text = item.detailedName()
            itemView.setOnClickListener {
                clicked(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchedItemViewHolder {
        return SearchedItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_search_result_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchedItemViewHolder, position: Int) {
        holder.bind(dataSource[position])
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }
}