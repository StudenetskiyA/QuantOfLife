     package com.skyfolk.quantoflife.ui.statistic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.StatisticFragmentBinding
import com.skyfolk.quantoflife.statistic.DayAxisValueFormatter
import com.skyfolk.quantoflife.statistic.WeekAxisValueFormatter
import java.util.*

import org.koin.android.viewmodel.ext.android.viewModel


class StatisticFragment : Fragment() {
    private val viewModel: StatisticViewModel by viewModel()
    private lateinit var binding: StatisticFragmentBinding

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
            val set = LineDataSet(data.entry, "")
            set.setDrawIcons(false)
            set.setDrawFilled(true)
            context?.let { set.fillDrawable = ContextCompat.getDrawable(it, R.drawable.fade_red) }

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set)
            val barData = LineData(dataSets)
            barData.setDrawValues(true)

            val xAxis = binding.chart.xAxis
            xAxis.position = XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(true)
            xAxis.labelRotationAngle = -45F

            xAxis.granularity = (data.entry[1].x - data.entry[0].x)
            xAxis.labelCount = data.entry.size

            val xAxisFormatter = WeekAxisValueFormatter(data.firstDate)
            xAxis.valueFormatter = xAxisFormatter

            binding.chart

            binding.chart.data = barData
        })

        return binding.root
    }
}