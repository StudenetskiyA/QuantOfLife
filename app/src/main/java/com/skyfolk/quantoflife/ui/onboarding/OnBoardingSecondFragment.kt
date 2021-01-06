package com.skyfolk.quantoflife.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.skyfolk.quantoflife.MainActivity
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.OnboardingFirstFragmentBinding
import com.skyfolk.quantoflife.databinding.OnboardingSecondFragmentBinding
import org.koin.android.viewmodel.ext.android.viewModel

class OnBoardingSecondFragment : Fragment() {
    private val viewModel: OnBoardingViewModel by viewModel()

    private lateinit var binding: OnboardingSecondFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = OnboardingSecondFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.navigationState.observe(viewLifecycleOwner, {
            when (it) {
                is OnBoardingWayEvent.OnBoardingCompleted -> {
                    startActivity(Intent(context, MainActivity::class.java))
                }
                else -> {}
            }
        })

        binding.toSecondStepButton.setOnClickListener {
            val categoryDefaultName = resources.getStringArray(R.array.category_name)
            viewModel.submitCategoryName(
                if (binding.firstCategoryName.text.isEmpty()) categoryDefaultName[0] else binding.firstCategoryName.text.toString(),
                if (binding.secondCategoryName.text.isEmpty()) categoryDefaultName[1] else binding.secondCategoryName.text.toString(),
                if (binding.thirdCategoryName.text.isEmpty()) categoryDefaultName[2] else binding.thirdCategoryName.text.toString(),
                categoryDefaultName[3]
            )
        }
    }
}