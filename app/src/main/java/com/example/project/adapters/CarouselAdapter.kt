package com.example.project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.CarouselItemBinding

class CarouselAdapter(
    private var dataList: List<AccountData>,
    var currentPagePosition: Int,
    private val iconList: List<Int> // List of icon resource IDs
) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    inner class CarouselViewHolder(val binding: CarouselItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val binding = CarouselItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return CarouselViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val accountData = dataList[position]
        val iconResourceId = iconList[position % iconList.size]

        val displayAccountType = when (accountData.accountType) {
            "COURANT" -> "Compte courant"
            else -> accountData.accountType
        }
        val displayAccountNumber = "Numéro de compte\n${accountData.accountNumber}"

        holder.binding.apply {
            tvAccountType.text = displayAccountType
            tvAccountNumber.text = displayAccountNumber
            tvAccountBalance.text = accountData.balance
            icon.setImageResource(iconResourceId)
        }
    }



    override fun getItemCount(): Int = dataList.size

    fun setData(dataList: List<AccountData>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }
}




data class AccountData(val accountType: String, val accountNumber: String, val balance: String)
