package com.example.project.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.RecentTransactionsItemBinding
import com.example.project.prototype.Transaction

class TransactionAdapter(private var transactions: List<Transaction>,
                         private val currentUserAccountId: String,
                         private val listener: OnRecycleViewItemClickListener
) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    interface OnRecycleViewItemClickListener {
        fun onItemClick(transactionData: Transaction)
    }
    inner class TransactionViewHolder(val binding: RecentTransactionsItemBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(transactions[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = RecentTransactionsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.itemView.context

        val formattedDate = transaction.date.split(" ")[0]

        holder.binding.txtDate.text = formattedDate
        holder.binding.txtTypeTransaction.text = transaction.typeTransaction

        if (transaction.compteEmet.id_proprietaire == currentUserAccountId) {
            holder.binding.txtAmount.text = "-${String.format("%.2f DH", transaction.montant)}"
            holder.binding.txtAmount.setTextColor(ContextCompat.getColor(context, com.example.project.R.color.usualColor))
            holder.binding.imgTransactionType.setImageResource(com.example.project.R.drawable.up_arrow)
            holder.binding.imgTransactionType.setColorFilter(ContextCompat.getColor(context, com.example.project.R.color.colorPrimary))


        } else {
            holder.binding.txtAmount.text = "+${String.format("%.2f DH", transaction.montant)}"
            holder.binding.txtAmount.setTextColor(ContextCompat.getColor(context, com.example.project.R.color.green))
            holder.binding.imgTransactionType.setImageResource(com.example.project.R.drawable.down_arrow)
            holder.binding.imgTransactionType.setColorFilter(ContextCompat.getColor(context, com.example.project.R.color.green))
// Set the image for incoming
        }
    }

    override fun getItemCount() = transactions.size


    fun getCurrentTransactions(): List<Transaction> {
        return transactions
    }


    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
        Log.d("TransactionsAdapter", "Transactions updated:s ${transactions.size}")

    }
}

