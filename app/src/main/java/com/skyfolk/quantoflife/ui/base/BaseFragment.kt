package com.skyfolk.quantoflife.ui.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.skyfolk.quantoflife.ui.statistic.StatisticFragmentState

abstract class BaseFragment<T: BaseViewModel<out ViewState>, V: ViewState, B: ViewDataBinding > : Fragment() {
    @get:LayoutRes
    protected abstract val layoutId: Int

    /**
     * by injectViewModel()
     */
    protected abstract val viewModel: T

    lateinit var binding: B

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //initViewModel()

        subscribeBaseViewModelEvents()
        subscribeBaseViewModelState()
        //  subscribeNavigationEvents()

//        lifecycle.addObserver(viewModel)
//
//        if (parentNotAllowHandleBackPress()) return
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    private fun subscribeBaseViewModelEvents() = observeOnViewLifecycle(viewModel.events) { event ->
        event.execute(this)
    }

    private fun subscribeBaseViewModelState() {
        Log.d("skyfolk-testViewModel","subscribeBaseViewModelState")

        viewModel.viewState.observe(viewLifecycleOwner,
            { state ->
                Log.d("skyfolk-testViewModel", "subscribeBaseViewModelState, state = ${state}")

                observeViewState(state as? V ?: throw error("not correct view state type"))
            })
    }

    abstract fun observeViewState(viewState: V)
}

abstract class BaseViewModel<V: ViewState> : ViewModel() {
    abstract val initialViewState: V

    private val _events = MutableLiveData<ViewEvent>()
    val events: LiveData<ViewEvent> = _events
    // val navEvents: LiveData<NavigationEvent> by lazy { _navEvents.merge(this, viewEventPublisher.navEvent) }
    // private val _navEvents = BufferLiveData<NavigationEvent>()

    private val _viewState = MutableLiveData<V>().apply {
        value = initialViewState
    }
    val viewState: LiveData<V> = _viewState

    fun postState(state: V?) {
        Log.d("skyfolk-testViewModel","post state = ${state}")

        _viewState.value = state
    }
}

interface ViewEvent {
    fun execute(fragment: Fragment)
}

interface ViewState {

}


inline fun <T, L : LiveData<T>> Fragment.observeOnViewLifecycle(
    liveData: L,
    crossinline body: (T) -> Unit
) =
    liveData.observe(this.viewLifecycleOwner, { it?.let(body) })