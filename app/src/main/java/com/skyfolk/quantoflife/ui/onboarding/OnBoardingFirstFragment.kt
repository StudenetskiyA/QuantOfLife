package com.skyfolk.quantoflife.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.OnboardingFirstFragmentBinding
import org.koin.android.viewmodel.ext.android.viewModel

class OnBoardingFirstFragment : Fragment() {
    private val viewModel: OnBoardingViewModel by viewModel()

    private lateinit var binding: OnboardingFirstFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = OnboardingFirstFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.navigationState.observe(viewLifecycleOwner, {
            when (it) {
                is OnBoardingWayEvent.GoToSecondScreen -> {
                    findNavController().navigate(R.id.action_go_to_second_screen)
                }
                else -> {}
            }
        })

        binding.toSecondStepButton.setOnClickListener {
            viewModel.goToSecondScreen()
        }
    }
}