package com.example.project.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ItemBeneficiaireBinding
import com.example.project.models.ClientAccountDetails
import com.example.project.viewmodels.ConsultationViewModel


class ClientAdapter(private var clientDetails: List<ClientAccountDetails>, private val consultationViewModel: ConsultationViewModel) : RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    class ClientViewHolder(private val binding: ItemBeneficiaireBinding, private val adapter: ClientAdapter, private val consultationViewModel: ConsultationViewModel) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val clientDetail = adapter.getClientDetail(adapterPosition)
                if (clientDetail != null) {
                    Log.d("ClientAdapter", "Client selected: $clientDetail")
                    consultationViewModel.selectClient(clientDetail)
                }
            }
        }

        fun bind(clientDetail: ClientAccountDetails) {
            Log.d("ClientAdapter", "Binding client detail: $clientDetail")
            binding.tvNom.text = "${clientDetail.nom.toUpperCase()} ${clientDetail.prenom}"
            binding.tvAccountNumber.text = "Numéro de compte: ${clientDetail.accountNumber}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ItemBeneficiaireBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(binding, this, consultationViewModel)
    }

    fun getClientDetail(position: Int): ClientAccountDetails? {
        return if (position != RecyclerView.NO_POSITION && position < clientDetails.size) clientDetails[position] else null
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(clientDetails[position])
    }

    override fun getItemCount() = clientDetails.size

    fun updateClientDetails(newClientDetails: List<ClientAccountDetails>) {
        Log.d("ClientAdapter", "Updating client details: $newClientDetails")
        clientDetails = newClientDetails
        notifyDataSetChanged()
    }
}
