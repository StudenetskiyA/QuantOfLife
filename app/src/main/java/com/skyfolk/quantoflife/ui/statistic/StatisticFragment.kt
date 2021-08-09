package com.skyfolk.quantoflife.ui.statistic

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.StatisticFragmentBinding
import com.skyfolk.quantoflife.statistic.IntervalAxisValueFormatter
import com.skyfolk.quantoflife.statistic.WeekAxisValueFormatter
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class StatisticFragment : Fragment() {
    private val viewModel: StatisticViewModel by viewModel()
    private lateinit var binding: StatisticFragmentBinding

    // Иначе дурацкие спиннеры лишние раз тригерятся
    private var selectedEventFilterName: String = "Все события"
    private var selectedEventFilterName2: String = "Все события"

    private var spinnerSelect = 0
    private var spinnerSelect2 = 0

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
                    binding.eventSpinnerLayout.visibility = View.INVISIBLE
                }
                is StatisticFragmentState.Entries -> {
                    if (data.entries.first().entry.size > 1) {
                        val set1 = LineDataSet(data.entries[0].entry, "")
                        set1.color = Color.rgb(104, 241, 175)
                        set1.setDrawFilled(true)
                        val set2 = LineDataSet(data.entries[1].entry, "")
                        set2.color = Color.rgb(164, 228, 251)
                        set2.setDrawFilled(true)
                        set1.setDrawIcons(false)
                        set2.setDrawIcons(false)
                        context?.let {
                            set1.fillDrawable =
                                ContextCompat.getDrawable(it, R.drawable.fade_red)
                            set2.fillDrawable =
                                ContextCompat.getDrawable(it, R.drawable.fade_green)
                        }

                        val xAxis = binding.chart.xAxis
                        xAxis.position = XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(true)
                        xAxis.labelRotationAngle = -45F
                        xAxis.granularity =
                            (data.entries[0].entry[1].x - data.entries[0].entry[0].x)
                        xAxis.labelCount = data.entries[0].entry.size

                        val xAxisFormatter = IntervalAxisValueFormatter(data.entries[0].firstDate, TimeInterval.Month)
                        xAxis.valueFormatter = xAxisFormatter

                        val data = LineData(set1, set2)
                        binding.chart.invalidate()
                        binding.chart.data = data

                        binding.progress.visibility = View.INVISIBLE
                        binding.chart.visibility = View.VISIBLE
                        binding.eventSpinnerLayout.visibility = View.VISIBLE
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
            binding.eventSpinner2.adapter = quantsSpinnerAdapter
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
                        viewModel.setSelectedEventFilter(
                            viewModel.getQuantIdByName(selectedEventFilterName),
                            viewModel.getQuantIdByName(selectedEventFilterName2)
                        )
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.eventSpinner2.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (selectedEventFilterName2 != parent.getItemAtPosition(position).toString()) {
                        selectedEventFilterName2 = parent.getItemAtPosition(position).toString()
                        viewModel.setSelectedEventFilter(
                            viewModel.getQuantIdByName(selectedEventFilterName),
                            viewModel.getQuantIdByName(selectedEventFilterName2)
                        )
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        viewModel.setSelectedEventFilter(
            viewModel.getQuantIdByName(selectedEventFilterName),
            viewModel.getQuantIdByName(selectedEventFilterName2)
        )

        return binding.root
    }
}