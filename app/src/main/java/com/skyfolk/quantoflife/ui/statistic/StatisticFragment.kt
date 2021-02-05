package com.skyfolk.quantoflife.ui.statistic

import com.skyfolk.quantoflife.ui.feeds.EventListDataAdapter
import com.skyfolk.quantoflife.ui.feeds.StatisticViewModel
import com.skyfolk.quantoflife.ui.feeds.TimeInterval


import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.skyfolk.quantoflife.databinding.FeedsFragmentBinding
import com.skyfolk.quantoflife.databinding.StatisticFragmentBinding
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.setOnHideByTimeout
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.ui.now.CreateEventDialogFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class StatisticFragment : Fragment() {
//    private val viewModel: StatisticViewModel by viewModel()
    private lateinit var binding: StatisticFragmentBinding
//    private val quantStorageInteractor: IQuantsStorageInteractor by inject()
//    private val settingsInteractor: SettingsInteractor by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StatisticFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
}