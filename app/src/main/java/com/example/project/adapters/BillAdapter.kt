package com.example.project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ItemBillBinding
import com.example.project.models.Bill

class BillAdapter(private val bills: List<Bill>, private val listener: OnItemClickListener?) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    private val selectedItems = mutableListOf<Int>()
    private var onSelectionChangeListener: ((Double) -> Unit)? = null

    inner class BillViewHolder(private val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    toggleSelection(position)
                    listener?.onItemClick(position)
                }
            }
        }

        fun bind(bill: Bill, position: Int) {
            binding.billNumber.text = bill.number
            binding.billAmount.text = "${bill.amount.toString()} DH"
            binding.billDueDate.text = bill.due_date

            // Set the custom selector state
            itemView.isSelected = selectedItems.contains(position)
            binding.customSelector.isSelected = selectedItems.contains(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBillBinding.inflate(inflater, parent, false)
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = bills[position]
        holder.bind(bill, position)
    }

    override fun getItemCount(): Int {
        return bills.size
    }

    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position)
        calculateTotalSelectedAmount()
    }


    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(bills.indices)
        notifyDataSetChanged()
        calculateTotalSelectedAmount()
    }

    fun deselectAll() {
        selectedItems.clear()
        notifyDataSetChanged()
        calculateTotalSelectedAmount()
    }

    private fun calculateTotalSelectedAmount() {
        val totalAmount = selectedItems.sumOf { bills[it].amount }
        onSelectionChangeListener?.invoke(totalAmount)
    }

    fun setOnSelectionChangeListener(listener: (Double) -> Unit) {
        onSelectionChangeListener = listener
    }

    fun getSelectedItems(): List<Bill> {
        return selectedItems.map { index -> bills[index] }
    }
}
