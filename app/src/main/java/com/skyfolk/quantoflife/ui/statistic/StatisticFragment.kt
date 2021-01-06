package com.skyfolk.quantoflife.ui.statistic

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skyfolk.quantoflife.databinding.StatisticFragmentBinding
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.settings.SettingsInteractor
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class StatisticFragment : Fragment() {
    private val viewModel: StatisticViewModel by viewModel()
    private lateinit var binding: StatisticFragmentBinding
    private val quantStorageInteractor: IQuantsStorageInteractor by inject()
    private val settingsInteractor: SettingsInteractor by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StatisticFragmentBinding.inflate(inflater, container, false)

        val listOfEvents: RecyclerView = binding.listOfEvents
        listOfEvents.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)

        val categoryArray = mutableListOf(settingsInteractor.getCategoryName(QuantCategory.Physical),
            settingsInteractor.getCategoryName(QuantCategory.Emotion),
            settingsInteractor.getCategoryName(QuantCategory.Evolution),
            settingsInteractor.getCategoryName(QuantCategory.Other)
        )
        binding.physicalDescription.text = "Всего ${categoryArray[0]} :"
        binding.emotionalDescription.text = "Всего ${categoryArray[1]} :"
        binding.evolutionDescription.text = "Всего ${categoryArray[2]} :"

        lifecycleScope.launch {
            viewModel.listOfEvents.observe(viewLifecycleOwner, {
                val adapter = EventListDataAdapter(it, quantStorageInteractor, settingsInteractor) {}
                if (adapter.itemCount == 0) {
                    binding.itemsNotFound.visibility = View.VISIBLE
                    binding.listOfEvents.visibility = View.GONE
                } else {
                    binding.itemsNotFound.visibility = View.GONE
                    binding.listOfEvents.visibility = View.VISIBLE
                }
                binding.totalDescription.text = "Итого за период найдено ${adapter.itemCount} событий."
                listOfEvents.adapter = adapter
            })

            viewModel.listOfQuants.observe(viewLifecycleOwner, { listOfQuants ->
                val listOfQuantName = listOfQuants.map { it.name }.toMutableList()
                listOfQuantName.add(0,"Все события")
                val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, listOfQuantName)
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                binding.spinner.adapter = adapter

                binding.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                        Log.d("log-level-skyfolk", "select = ${parent.getItemAtPosition(position)}")
                        if (position == 0) {
                            viewModel.setSelectedEventFilter(null)
                        } else {
                            viewModel.setSelectedEventFilter(
                                parent.getItemAtPosition(position).toString()
                            )
                        }
                        viewModel.runSearch()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>){}
                }
            })

            viewModel.totalPhysicalFound.observe(viewLifecycleOwner, {
                binding.physicalValue.text = String.format("%.1f", it)
            })

            viewModel.totalEmotionalFound.observe(viewLifecycleOwner, {
                binding.emotionalValue.text = String.format("%.1f", it)
            })

            viewModel.totalEvolutionFound.observe(viewLifecycleOwner, {
                binding.evolutionValue.text = String.format("%.1f", it)
            })

            viewModel.totalFound.observe(viewLifecycleOwner, {
                binding.totalValue.text = String.format("%.1f", it)
            })

            when (viewModel.selectedTimeInterval) {
                TimeInterval.Today -> binding.timeIntervalRadioGroup.check(binding.timeIntervalToday.id)
                TimeInterval.Week -> binding.timeIntervalRadioGroup.check(binding.timeIntervalThisWeek.id)
                TimeInterval.Month -> binding.timeIntervalRadioGroup.check(binding.timeIntervalThisMonth.id)
                else -> binding.timeIntervalRadioGroup.check(binding.timeIntervalAll.id)
            }
        }

        binding.timeIntervalRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            var selected = TimeInterval.All
            when (checkedId) {
                binding.timeIntervalThisMonth.id -> {
                    selected = TimeInterval.Month
                }
                binding.timeIntervalThisWeek.id -> {
                    selected = TimeInterval.Week
                }
                binding.timeIntervalToday.id -> {
                    selected = TimeInterval.Today
                }
            }
            viewModel.saveTimeIntervalState(selected)
            viewModel.runSearch()
        }

        binding.search.setOnClickListener {

        }

        binding.clearSearch.setOnClickListener {
            viewModel.clearSearch()
        }

        return binding.root
    }
}