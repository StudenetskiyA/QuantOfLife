package com.skyfolk.quantoflife.ui.now

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.entity.QuantBase

class QuantListDataAdapter(private val quantsList: List<QuantBase>,
                           private val clickListener: (QuantBase) -> Unit,
                           private val longClickListener: (QuantBase) -> Boolean) : RecyclerView.Adapter<QuantListDataAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_quant, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(quantsList[position], clickListener, longClickListener)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return quantsList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(quant: QuantBase,
                      clickListener: (QuantBase) -> Unit,
                      longClickListener: (QuantBase) -> Boolean) {
            val textViewName = itemView.findViewById(R.id.quant_name) as TextView
            textViewName.text = quant.name

            val image = itemView.findViewById(R.id.quant_image) as ImageView
            val imageResource = itemView.context.resources.getIdentifier(quant.icon, "drawable", itemView.context.packageName)
            if (imageResource !=0 ) {
                image.setImageResource(imageResource)
            } else {
                image.setImageResource(itemView.context.resources.getIdentifier("quant_default", "drawable", itemView.context.packageName))
            }


            itemView.setOnClickListener { clickListener(quant) }
            itemView.setOnLongClickListener { longClickListener(quant) }
        }
    }
}