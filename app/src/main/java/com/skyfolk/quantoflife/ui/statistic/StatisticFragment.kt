     package com.skyfolk.quantoflife.ui.statistic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.StatisticFragmentBinding
import com.skyfolk.quantoflife.statistic.DayAxisValueFormatter
import com.skyfolk.quantoflife.statistic.WeekAxisValueFormatter
import java.util.*

import org.koin.android.viewmodel.ext.android.viewModel


class StatisticFragment : Fragment() {
    private val viewModel: StatisticViewModel by viewModel()
    private lateinit var binding: StatisticFragmentBinding
    // Иначе дурацкие спиннеры лишние раз тригерятся
    private var selectedEventFilterName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StatisticFragmentBinding.inflate(inflater, container, false)

        binding.chart.description.isEnabled = false
        binding.chart.setPinchZoom(false)
        binding.chart.setDrawGridBackground(false)

        binding.chart.legend.isEnabled = false

        viewModel.barEntryData.observe(viewLifecycleOwner, { data ->
            when (data) {
                is StatisticFragmentState.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.chart.visibility = View.INVISIBLE
                    binding.eventSpinner.visibility = View.INVISIBLE
                }
                is StatisticFragmentState.EntryAndFirstDate -> {
                    if (data.entry.size > 1) {
                        val set = LineDataSet(data.entry, "")
                        set.setDrawIcons(false)
                        set.setDrawFilled(true)
                        context?.let {
                            set.fillDrawable = ContextCompat.getDrawable(it, R.drawable.fade_red)
                        }

                        val dataSets = ArrayList<ILineDataSet>()
                        dataSets.add(set)
                        val barData = LineData(dataSets)
                        barData.setDrawValues(true)

                        val xAxis = binding.chart.xAxis
                        xAxis.position = XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(true)
                        xAxis.labelRotationAngle = -45F

                        xAxis.granularity = (data.entry[1].x - data.entry[0].x)
                        QLog.d("skyfolk-statistic","granularity = ${ (data.entry[1].x - data.entry[0].x)}")

                        xAxis.labelCount = data.entry.size

                        val xAxisFormatter = WeekAxisValueFormatter(data.firstDate)
                        xAxis.valueFormatter = xAxisFormatter

                        binding.chart.invalidate()
                        binding.chart.data = barData

                        binding.progress.visibility = View.INVISIBLE
                        binding.chart.visibility = View.VISIBLE
                        binding.eventSpinner.visibility = View.VISIBLE
                    } else {
                        // Not enough data
                        binding.progress.visibility = View.INVISIBLE
                        binding.chart.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.listOfQuants.observe(viewLifecycleOwner, { listOfQuants ->
            // List of quants for spinner
            val listOfQuantName = listOfQuants.map { it.name }.toMutableList()
            listOfQuantName.add(0, "Все события")
            val quantsSpinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOfQuantName
            )
            quantsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.eventSpinner.adapter = quantsSpinnerAdapter
        })

        binding.eventSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (selectedEventFilterName != parent.getItemAtPosition(position).toString()) {
                        selectedEventFilterName = parent.getItemAtPosition(position).toString()
                        if (position == 0) {
                            viewModel.setSelectedEventFilter(null)
                        } else {
                            viewModel.setSelectedEventFilter(
                                viewModel.getQuantIdByName(
                                    parent.getItemAtPosition(position).toString()
                                )
                            )
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        return binding.root
    }
}