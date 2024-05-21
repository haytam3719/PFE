package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.BillAdapter
import com.example.project.adapters.OnItemClickListener
import com.example.project.databinding.Payment3Binding
import com.example.project.models.Bill
import com.example.project.viewmodels.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentStepThree :Fragment(), OnItemClickListener {

    private var _binding: Payment3Binding? = null
    private val binding get() = _binding!!
    private val paymentViewModel: PaymentViewModel by viewModels()
    private lateinit var adapter: BillAdapter
    private var isAllSelected = false
    private var totalApayer:Double = 0.0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Payment3Binding.inflate(inflater, container, false)
        binding.paymentStepThree = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = binding.recyclerViewInvoices

        paymentViewModel.bills.observe(viewLifecycleOwner, Observer<List<Bill>> { bills ->
            Log.e("debug bills", bills.toString())
            adapter = BillAdapter(bills,this)

            adapter.setOnSelectionChangeListener { totalAmount ->
                binding.tvTotalAmount.text = "${(totalAmount+0.25*totalAmount).toString()} DH"
                binding.tvFeeAmount.text = "${(0.25*totalAmount).toString()} DH"
                totalApayer = totalAmount+0.25*totalAmount
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            recyclerView.adapter = adapter
        })

        paymentViewModel.getBills()


        binding.ivSelectAll.setOnClickListener {
            if (isAllSelected) {
                adapter.deselectAll()
                isAllSelected = false
                binding.ivSelectAll.isSelected = false
            } else {
                adapter.selectAll()
                isAllSelected = true
                binding.ivSelectAll.isSelected = true
            }
        }

    }



    fun onClickContinuer(view: View) {
        val selectedBills = adapter.getSelectedItems()
        Log.d("onClickContinuer", "Selected bills: $selectedBills")

        if (selectedBills.isNotEmpty()) {
            val ids = selectedBills.map { it.number }.toTypedArray()
            val amounts = selectedBills.map { it.amount }.toDoubleArray()
            val dueDates = selectedBills.map { it.due_date }.toTypedArray()
            val bundle = Bundle().apply {
                putStringArray("numbers", ids)
                putDoubleArray("amounts", amounts)
                putStringArray("dueDates", dueDates)
                putString("totalApayer","${totalApayer} DH")
            }
            findNavController().navigate(R.id.paymentStepThree_to_paymentStepFour, bundle)
        } else {
            Toast.makeText(requireContext(), "Veuillez séléctioner au moins une facture à payer", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClick(position: Int) {
        val selectedItems = adapter.getSelectedItems()
        if (position in selectedItems.indices) {
            val selectedBill = selectedItems[position]
            val billAmount = selectedBill.amount
            Toast.makeText(requireContext(), "Montant séléctioné: $billAmount DH", Toast.LENGTH_SHORT).show()
        }
    }
}