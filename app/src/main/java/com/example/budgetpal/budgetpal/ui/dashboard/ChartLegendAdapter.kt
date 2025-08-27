package com.example.budgetpal.budgetpal.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetpal.budgetpal.databinding.ItemChartLegendBinding

class ChartLegendAdapter : RecyclerView.Adapter<ChartLegendAdapter.LegendViewHolder>() {

    private var items = listOf<LegendItem>()

    fun updateItems(newItems: List<LegendItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendViewHolder {
        val binding = ItemChartLegendBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LegendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LegendViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class LegendViewHolder(private val binding: ItemChartLegendBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: LegendItem) {
            binding.colorBox.setBackgroundColor(item.color)
            binding.tvCategory.text = item.name
            binding.tvPercentage.text = "${item.percentage}%"
        }
    }

    data class LegendItem(
        val color: Int,
        val name: String,
        val percentage: Float
    )
} 