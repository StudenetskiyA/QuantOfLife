package com.skyfolk.quantoflife.ui.create_quant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.skyfolk.quantoflife.R

class IconsListDataAdapter(
    private val quantsList: ArrayList<String>,
    private var selectedItem: String?,
    private val clickListener: (String) -> Unit
) : RecyclerView.Adapter<IconsListDataAdapter.ViewHolder>() {
    private var selectedItemIndex: Int? = quantsList.indexOf(selectedItem)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_icon, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(quantsList[position], selectedItemIndex == position, clickListener)
    }

    override fun getItemCount(): Int {
        return quantsList.size
    }

    fun update(selectedItemIndex: Int?) {
        this.selectedItemIndex = selectedItemIndex
        this.notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(
            quantImage: String, isSelected: Boolean,
            clickListener: (String) -> Unit
        ) {
            val image = itemView.findViewById(R.id.quant_image) as ImageView
            val imageResource = itemView.context.resources.getIdentifier(quantImage, "drawable", itemView.context.packageName)
            if (imageResource !=0 ) {
                image.setImageResource(imageResource)
            } else {
                image.setImageResource(itemView.context.resources.getIdentifier("quant_default", "drawable", itemView.context.packageName))
            }

            itemView.setOnClickListener { clickListener(quantImage) }

            val imageSelected = itemView.findViewById(R.id.quant_is_selected) as ImageView
            imageSelected.visibility = if (isSelected)  View.VISIBLE else View.GONE
        }
    }
}
