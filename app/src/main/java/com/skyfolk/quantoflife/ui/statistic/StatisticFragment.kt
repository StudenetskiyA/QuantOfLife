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
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.StatisticFragmentBinding
import com.skyfolk.quantoflife.meansure.Measure
import com.skyfolk.quantoflife.meansure.QuantFilter
import com.skyfolk.quantoflife.meansure.fromPositionToMeasure
import com.skyfolk.quantoflife.statistic.IntervalAxisValueFormatter
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.utils.fromPositionToTimeInterval
import org.koin.android.viewmodel.ext.android.viewModel

class StatisticFragment : Fragment() {
    private val viewModel: StatisticViewModel by viewModel()
    private lateinit var binding: StatisticFragmentBinding

    // Иначе дурацкие спиннеры лишние раз тригерятся
    private var selectedEventFilterName: QuantFilter? = null
    private var selectedEventFilterName2: QuantFilter? = null
    private var selectedEventTimeInterval: TimeInterval? = null
    private var selectedMeasure: Measure? = null

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
                    if (data.entries.size > 0 && data.entries.first().entry.size > 1) {
                        val dataSets = arrayListOf<LineDataSet>()
                        val set1 = LineDataSet(data.entries[0].entry, "")
                        set1.color = Color.rgb(104, 241, 175)
                        set1.setDrawFilled(true)
                        set1.setDrawIcons(false)
                        context?.let {
                            set1.fillDrawable =
                                ContextCompat.getDrawable(it, R.drawable.fade_red)
                        }
                        dataSets.add(set1)

                        if (data.entries.size > 1) {
                            val set2 = LineDataSet(data.entries[1].entry, "")
                            set2.color = Color.rgb(164, 228, 251)
                            set2.setDrawFilled(true)
                            set2.setDrawIcons(false)
                            context?.let {
                                set2.fillDrawable =
                                    ContextCompat.getDrawable(it, R.drawable.fade_green)
                            }
                            dataSets.add(set2)
                        }

                        val xAxis = binding.chart.xAxis
                        xAxis.position = XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(true)
                        xAxis.labelRotationAngle = -60F

                        //TODO Даже в три раза меньше делений это может быть много, сделай нормально
                        xAxis.granularity = if (data.entries[0].entry.size > 20) {
                            (data.entries[0].entry[1].x - data.entries[0].entry[0].x) * 4
                        } else {
                            (data.entries[0].entry[1].x - data.entries[0].entry[0].x)
                        }
                        xAxis.labelCount = data.entries[0].entry.size

                        selectedEventTimeInterval?.let {
                            val xAxisFormatter = viewModel.getFormatter(data.entries[0].firstDate, it)
                            xAxis.valueFormatter = xAxisFormatter
                        }

                        val dataForGraph = LineData(dataSets.toList())
                        binding.chart.invalidate()
                        binding.chart.data = dataForGraph

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

        viewModel.selectedMeasure.observe(viewLifecycleOwner, {
            binding.meansureSpinner.setSelection(it.toPosition())
        })

        viewModel.selectedTimeInterval.observe(viewLifecycleOwner, {
            binding.timePeriodSpinner.setSelection(it.toGraphPosition())
        })

        viewModel.selectedFirstQuantFilter.observe(viewLifecycleOwner, {
            binding.eventSpinner.setSelection(it.toGraphPosition())
        })

        viewModel.selectedSecondQuantFilter.observe(viewLifecycleOwner, {
            binding.eventSpinner2.setSelection(it.toGraphPosition())
        })

        viewModel.listOfQuants.observe(viewLifecycleOwner, { listOfQuants ->
            // List of quants for spinner
            val listOfQuantName = listOfQuants.map { it.name }.toMutableList()
            listOfQuantName.add(0, "Ничего")
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
                    val newSelectedEventFilterName = when (position) {
                        0 -> QuantFilter.All
                        1 -> QuantFilter.Nothing
                        else -> QuantFilter.OnlySelected(parent.getItemAtPosition(position).toString())
                    }
                    if (selectedEventFilterName != newSelectedEventFilterName) {
                        selectedEventFilterName = newSelectedEventFilterName
                        setSelectedFilter()
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
                    val newSelectedEventFilterName = when (position) {
                        0 -> QuantFilter.All
                        1 -> QuantFilter.Nothing
                        else -> QuantFilter.OnlySelected(parent.getItemAtPosition(position).toString())

                    }
                    if (selectedEventFilterName2 != newSelectedEventFilterName) {
                        selectedEventFilterName2 = newSelectedEventFilterName
                        setSelectedFilter()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.meansureSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (selectedMeasure != position.fromPositionToMeasure()) {
                        QLog.d("skyfolk-graph","position = $position")
                        selectedMeasure = position.fromPositionToMeasure()
                        setSelectedFilter()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.timePeriodSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val interval = position.fromPositionToTimeInterval()
                    if (selectedEventTimeInterval != interval) {
                        selectedEventTimeInterval = interval
                        setSelectedFilter()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        setSelectedFilter()

        return binding.root
    }

    private fun setSelectedFilter() {
        viewModel.setSelectedEventFilter(
            selectedEventFilterName,
            selectedEventFilterName2,
            selectedEventTimeInterval,
            selectedMeasure
        )
    }
}