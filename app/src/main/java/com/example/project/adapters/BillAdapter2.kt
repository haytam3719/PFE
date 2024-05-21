package com.example.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ItemBillBinding
import com.example.project.models.Bill

class BillAdapter2(private val bills: List<Bill>) : RecyclerView.Adapter<BillAdapter2.BillViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillAdapter2.BillViewHolder {
        val binding = ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = bills[position]
        holder.bind(bill)
    }

    override fun getItemCount(): Int = bills.size

    inner class BillViewHolder(private val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bill: Bill) {
            binding.billNumber.text = bill.number
            binding.billAmount.text = "${bill.amount} DH"
            binding.billDueDate.text = bill.due_date
            binding.customSelector.visibility = View.INVISIBLE
        }
    }
}
