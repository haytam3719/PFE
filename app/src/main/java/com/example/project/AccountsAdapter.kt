package com.example.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.adapters.AccountData
import com.example.project.databinding.ItemCompteBinding

class AccountsAdapter(private val onClick: (AccountData) -> Unit) : RecyclerView.Adapter<AccountsAdapter.AccountViewHolder>() {
    var accounts: List<AccountData> = listOf()

    class AccountViewHolder(private val binding: ItemCompteBinding, private val onClick: (AccountData) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: AccountData) {
            with(binding) {
                textViewAccountType.text = formatAccountType(account.accountType)
                textViewAccountNumber.text = "Numero: ${account.accountNumber}"
                textViewAccountBalance.text = "Solde: ${account.balance} DH"
                root.setOnClickListener { onClick(account) }
            }
        }

        private fun formatAccountType(type: String): String {
            return when (type) {
                "CHEQUES" -> "Compte chèques"
                "COURANT" -> "Compte courant"
                "EPARGNE" -> "Compte épargne"
                else -> type // Default case to handle unexpected types
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCompteBinding.inflate(layoutInflater, parent, false)
        return AccountViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(accounts[position])
    }

    override fun getItemCount() = accounts.size

    fun updateAccounts(newAccounts: List<AccountData>) {
        accounts = newAccounts
        notifyDataSetChanged()
    }
}
