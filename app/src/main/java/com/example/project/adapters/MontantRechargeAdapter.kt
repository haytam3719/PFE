package com.example.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R

class MontantRechargeAdapter(
    private val amounts: List<String>,
    private val listener: OnAmountSelectedListener
) : RecyclerView.Adapter<MontantRechargeAdapter.MontantViewHolder>() {

    private var selectedPosition = -1

    interface OnAmountSelectedListener {
        fun onAmountSelected(amount: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MontantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_montant_recharge, parent, false)
        return MontantViewHolder(view)
    }

    override fun onBindViewHolder(holder: MontantViewHolder, position: Int) {
        holder.bind(amounts[position], position)
    }

    override fun getItemCount(): Int = amounts.size

    fun getSelectedAmount(): String? {
        return if (selectedPosition != -1) amounts[selectedPosition] else null
    }

    inner class MontantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val radioButtonAmount: RadioButton = itemView.findViewById(R.id.radioButtonAmount)
        private val textViewAmount: TextView = itemView.findViewById(R.id.textViewAmount)

        fun bind(amount: String, position: Int) {
            textViewAmount.text = amount
            radioButtonAmount.isChecked = position == selectedPosition

            radioButtonAmount.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
                listener.onAmountSelected(amount)
            }

            itemView.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
                listener.onAmountSelected(amount)
            }
        }
    }
}
