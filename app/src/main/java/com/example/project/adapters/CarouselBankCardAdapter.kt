package com.example.project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ItemCarteCarouselBinding
import com.example.project.models.CarteImpl

class CardAdapter(private var cards: List<CarteImpl>, private val itemClickListener:OnItemClickListener) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(carte: CarteImpl)
    }

    class CardViewHolder(private val binding: ItemCarteCarouselBinding,private val itemClickListener:OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bankCard: CarteImpl) {
            with(binding) {
                tvCardNumber.text = bankCard.numeroCarte
                tvCardHolderName.text = bankCard.nomTitulaire
                tvExpiryDate.text = bankCard.dateExpiration
                root.setOnClickListener { itemClickListener.onItemClick(bankCard) }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCarteCarouselBinding.inflate(layoutInflater, parent, false)
        return CardViewHolder(binding,itemClickListener)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount(): Int = cards.size

    fun updateCards(newCards: List<CarteImpl>) {
        cards = newCards
        notifyDataSetChanged()
    }
}
