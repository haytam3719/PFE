package com.example.project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.BillAdapter
import com.example.project.adapters.OnItemClickListener
import com.example.project.databinding.PaymentBinding
import com.example.project.models.Bill
import com.example.project.models.PaymentRequest
import com.example.project.viewmodels.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class Payment : Fragment(),OnItemClickListener {
    private val paymentViewModel: PaymentViewModel by viewModels()
    private lateinit var binding: PaymentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.recyclerView

        paymentViewModel.bills.observe(viewLifecycleOwner, Observer<List<Bill>> { bills ->
            Log.e("debug bills", bills.toString())
            val adapter = BillAdapter(bills, this)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            recyclerView.adapter = adapter
        })

        paymentViewModel.getBills()

    }


    override fun onItemClick(position: Int) {
        val clickedBill = paymentViewModel.bills.value?.get(position)
        clickedBill?.let {
            val paymentRequest = PaymentRequest(
                clickedBill.id,
                clickedBill.amount,
                clickedBill.due_date,
                clickedBill.number
            )
            paymentViewModel.makePaiement(clickedBill.amount, clickedBill.number)

            paymentViewModel.requestPayment(paymentRequest)


            clickedBill.let {
                val requestData = JSONObject().apply {
                    put("bill_id", clickedBill.id)
                    put("amount", clickedBill.amount)
                }

                val jsonStr = Uri.encode(requestData.toString())

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data =
                        Uri.parse("https://payment-gatewayapi.onrender.com/initiate_square_oauth?data=$jsonStr")
                }

                startActivity(intent)
            }
        }

    }}