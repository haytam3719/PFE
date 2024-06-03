package com.example.project.adapters

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project.databinding.ItemBeneficiaireBinding
import com.example.project.models.ClientAccountDetails
import com.example.project.viewmodels.MesBeneficiairesViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


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

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef = clientDetail.profileImageUrl?.let { storageRef.child(it) }
                    val imageData = imageRef?.getBytes(Long.MAX_VALUE)?.await()
                    val bitmap = imageData?.let { BitmapFactory.decodeByteArray(imageData, 0, it.size) }
                    bitmap?.let {
                        Glide.with(binding.imageView.context)
                            .load(it)
                            .circleCrop()
                            .into(binding.imageView)
                    }
                } catch (e: Exception) {
                    Log.e("ClientAdapter", "Error fetching image", e)
                }
            }
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
