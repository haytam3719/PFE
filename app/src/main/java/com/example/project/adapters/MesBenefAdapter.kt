package com.example.project.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ItemBeneficiaireBinding
import com.example.project.models.ClientAccountDetails
import com.example.project.viewmodels.MesBeneficiairesViewModel


class MesBenefAdapter(private var clientDetails: List<ClientAccountDetails>, private val mesBeneficiairesViewModel: MesBeneficiairesViewModel) : RecyclerView.Adapter<MesBenefAdapter.MesBenefViewHolder>() {

    class MesBenefViewHolder(private val binding: ItemBeneficiaireBinding, private val adapter: MesBenefAdapter, private val mesBeneficiairesViewModel: MesBeneficiairesViewModel) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val clientDetail = adapter.getClientDetail(adapterPosition)
                if (clientDetail != null) {
                    Log.d("ClientAdapter", "Client selected: $clientDetail")
                    mesBeneficiairesViewModel.selectClient(clientDetail)
                }
            }
        }

        fun bind(clientDetail: ClientAccountDetails) {
            Log.d("ClientAdapter", "Binding client detail: $clientDetail")
            binding.tvNom.text = "${clientDetail.nom.toUpperCase()} ${clientDetail.prenom}"
            binding.tvAccountNumber.text = "Numéro de compte: ${clientDetail.accountNumber}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesBenefViewHolder {
        val binding = ItemBeneficiaireBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MesBenefViewHolder(binding, this, mesBeneficiairesViewModel)
    }

    fun getClientDetail(position: Int): ClientAccountDetails? {
        return if (position != RecyclerView.NO_POSITION && position < clientDetails.size) clientDetails[position] else null
    }

    override fun onBindViewHolder(holder: MesBenefViewHolder, position: Int) {
        holder.bind(clientDetails[position])
    }

    override fun getItemCount() = clientDetails.size

    fun updateClientDetails(newClientDetails: List<ClientAccountDetails>) {
        Log.d("ClientAdapter", "Updating client details: $newClientDetails")
        clientDetails = newClientDetails
        notifyDataSetChanged()
    }
}
