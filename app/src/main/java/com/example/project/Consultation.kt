package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.project.adapters.AccountData
import com.example.project.adapters.CarouselAdapter
import com.example.project.adapters.CarouselPageTransformer
import com.example.project.databinding.ConsultationBinding
import com.example.project.viewmodels.ConsultationViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Consultation : Fragment() {

    private val consultationViewModel: ConsultationViewModel by viewModels()
    private lateinit var binding: ConsultationBinding
    private val iconList = listOf(R.drawable.note, R.drawable.note, R.drawable.note, R.drawable.note)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ConsultationBinding.inflate(inflater, container, false).apply {
            consultation = this@Consultation
            viewPager.apply {
                adapter = CarouselAdapter(emptyList(), 0, iconList)

                val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
                val offsetPx = resources.getDimensionPixelOffset(R.dimen.offset)
                val increasedOffsetPx = resources.getDimensionPixelOffset(R.dimen.increased_offset)
                setPadding(increasedOffsetPx, 0, increasedOffsetPx, 0)
                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = 3

                setPageTransformer(CarouselPageTransformer())
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        (adapter as? CarouselAdapter)?.currentPagePosition = position
                        adapter?.notifyDataSetChanged()
                    }
                })
            }
        }

        consultationViewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            if (accounts.isNotEmpty()) {
                updateAccountsDisplay(accounts)
                Log.d("ConsultationFragment", "Accounts updated in UI: $accounts")
            } else {
                Log.d("ConsultationFragment", "No accounts to display")
            }
        }

        FirebaseAuth.getInstance().currentUser?.let {
            consultationViewModel.fetchAccountsForCurrentUser(
                it.uid)
        }


        return binding.root
    }


    private fun updateAccountsDisplay(accounts: List<AccountData>) {
        (binding.viewPager.adapter as? CarouselAdapter)?.setData(accounts)
    }

    fun onClickAddAccount(view: View) {
        findNavController().navigate(R.id.consultation_to_createaccount)
    }
}
