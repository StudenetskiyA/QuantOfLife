package com.skyfolk.quantoflife.ui.base

import android.util.Log
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.TestFragmentBinding
import org.koin.android.viewmodel.ext.android.viewModel

class TestFragment: BaseFragment<TestViewModel, TestFragmentState, TestFragmentBinding>() {
    override val layoutId: Int = R.layout.test_fragment
    override val viewModel: TestViewModel by viewModel()

    override fun observeViewState(viewState: TestFragmentState) {
        binding.testMessage.text = viewState.message
    }

    override fun onResume() {
        super.onResume()
        Log.d("skyfolk-testViewModel","onResume")
        viewModel.start()
    }
}

class TestViewModel: BaseViewModel<TestFragmentState>() {
    override val initialViewState: TestFragmentState
        get() = TestFragmentState("default")

    fun start() {
        postState(viewState.value?.copy(message = "new message"))
    }
}

data class TestFragmentState(val message: String) : ViewState
