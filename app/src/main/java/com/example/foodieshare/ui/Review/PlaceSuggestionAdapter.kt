package com.example.foodieshare.ui.Review

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.foodieshare.R

data class PlaceSuggestion(
    val id: String,
    val primaryText: String,
    val secondaryText: String
) {
    override fun toString(): String = primaryText
}

class PlaceSuggestionAdapter(context: Context) :
    ArrayAdapter<PlaceSuggestion>(context, R.layout.item_place_suggestion) {

    private var suggestions: List<PlaceSuggestion> = emptyList()

    fun updateData(newData: List<PlaceSuggestion>) {
        suggestions = newData
        clear()
        addAll(newData)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_place_suggestion, parent, false)
        
        val item = getItem(position)
        val tvPrimary = view.findViewById<TextView>(R.id.tvPrimaryText)
        val tvSecondary = view.findViewById<TextView>(R.id.tvSecondaryText)
        
        tvPrimary.text = item?.primaryText
        tvSecondary.text = item?.secondaryText
        
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                results.values = suggestions
                results.count = suggestions.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                // We handle data updates via updateData() from the Fragment
            }
        }
    }
}