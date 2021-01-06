package com.skyfolk.quantoflife.ui.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skyfolk.quantoflife.R
import org.koin.android.viewmodel.ext.android.viewModel

class OnBoardingActivity : AppCompatActivity() {

    private val viewModel: OnBoardingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_onboarding)

       // tracksViewModel.start()
    }
}