package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.project.databinding.FragmentStepContainerBinding

class Questionnaire : Fragment() {

    private var _binding: FragmentStepContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepContainerBinding.inflate(inflater, container, false)
        val rootView = binding.root

        return rootView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val stepView = binding.stepView
        val viewPager = binding.viewPager
        //stepView.done(true)
        stepView.setStepsNumber(6)


        val fragments = listOf(Questionnaire1(),Questionnaire2(),Questionnaire3(),Questionnaire4(),Questionnaire5(),Questionnaire6())

        val adapter = object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                // Return the fragment for the corresponding step
                return fragments[position]
            }

            override fun getCount(): Int {
                // Return the total number of steps
                return fragments.size
            }
        }

        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Not needed
            }

            override fun onPageSelected(position: Int) {
                // Update the StepView to the current step
                stepView.go(position, true)
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Not needed
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
