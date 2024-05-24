package com.example.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.models.CarteImpl

class CardsAdapter(
    private var cardsList: List<CarteImpl>,
    private val onCardClick: (CarteImpl) -> Unit
) : RecyclerView.Adapter<CardsAdapter.CardViewHolder>() {

    fun updateCards(newCardsList: List<CarteImpl>) {
        cardsList = newCardsList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val currentCard = cardsList[position]
        holder.cardNumber.text = "N°: **** **** **** ${currentCard.numeroCarte.takeLast(4)}"
        holder.cardHolderName.text = "Titulaire: ${currentCard.nomTitulaire}"
        holder.expiryDate.text = "Expire le ${currentCard.dateExpiration}"
        holder.cardType.text = if (currentCard.numeroCompte != null) "Type: Carte Débit" else "Type: Carte Crédit"

        holder.itemView.setOnClickListener {
            onCardClick(currentCard)
        }
    }

    override fun getItemCount() = cardsList.size

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardNumber: TextView = itemView.findViewById(R.id.tv_card_number)
        val cardHolderName: TextView = itemView.findViewById(R.id.tv_card_holder_name)
        val expiryDate: TextView = itemView.findViewById(R.id.tv_expiry_date)
        val cardType: TextView = itemView.findViewById(R.id.tv_card_type)
    }
}
