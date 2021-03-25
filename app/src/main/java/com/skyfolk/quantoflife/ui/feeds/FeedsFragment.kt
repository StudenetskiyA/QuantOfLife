package com.skyfolk.quantoflife.ui.feeds

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.skyfolk.quantoflife.databinding.FeedsFragmentBinding
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.utils.setOnHideByTimeout
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.ui.now.CreateEventDialogFragment
import com.skyfolk.quantoflife.utils.toDateWithoutHourAndMinutes
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class FeedsFragment : Fragment() {
    private val viewModel: FeedsViewModel by viewModel()
    private lateinit var binding: FeedsFragmentBinding
    private val quantStorageInteractor: IQuantsStorageInteractor by inject()
    private val settingsInteractor: SettingsInteractor by inject()

    private val startIntervalCalendar = Calendar.getInstance()
    private val endIntervalCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FeedsFragmentBinding.inflate(inflater, container, false)

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
            viewModel.selectedTimeInterval.observe(viewLifecycleOwner, {
                when (it) {
                    is TimeInterval.Today -> {
                        binding.timeIntervalRadioGroup.check(binding.timeIntervalToday.id)
                        binding.timeIntervalSpinner.setSelection(0)
                    }
                    is TimeInterval.Week -> {
                        binding.timeIntervalRadioGroup.check(binding.timeIntervalThisWeek.id)
                        binding.timeIntervalSpinner.setSelection(1)
                    }
                    is TimeInterval.Month -> {
                        binding.timeIntervalRadioGroup.check(binding.timeIntervalThisMonth.id)
                        binding.timeIntervalSpinner.setSelection(2)
                    }
                    is TimeInterval.Selected -> {
                        binding.timeIntervalRadioGroup.check(binding.timeIntervalThisMonth.id)
                        binding.timeIntervalSpinner.setSelection(4)
                        binding.selectedTimeIntervalStartLabel.text = it.start.toDateWithoutHourAndMinutes()
                        binding.selectedTimeIntervalEndLabel.text = it.end.toDateWithoutHourAndMinutes()
                        startIntervalCalendar.timeInMillis = it.start
                        endIntervalCalendar.timeInMillis = it.end
                    }
                    else -> {
                        binding.timeIntervalRadioGroup.check(binding.timeIntervalAll.id)
                        binding.timeIntervalSpinner.setSelection(3)
                    }
                }
                binding.selectedTimeIntervalLayout.visibility = if (it is TimeInterval.Selected)  View.VISIBLE else View.INVISIBLE
            })

            viewModel.listOfEvents.observe(viewLifecycleOwner, {
                val adapter = EventListDataAdapter(it, quantStorageInteractor, settingsInteractor) { event ->
                    quantStorageInteractor.getQuantById(event.quantId)?.let { quant ->
                        val dialog = CreateEventDialogFragment(quant, event)

                        dialog.setDialogListener(object : CreateEventDialogFragment.DialogListener {
                            override fun onConfirm(event: EventBase, name: String) {
                                val snackBar = Snackbar.make(
                                    requireActivity().findViewById(R.id.content),
                                    "Событие '${name}' изменено",
                                    Snackbar.LENGTH_LONG
                                )
                                snackBar.setAction("Отмена") {
                                }
                                snackBar.setOnHideByTimeout {
                                    viewModel.eventEdited(event)
                                }
                                snackBar.show()
                            }

                            override fun onDecline() {
                            }

                            override fun onDelete(event: EventBase, name: String) {
                                val snackBar = Snackbar.make(
                                    requireActivity().findViewById(R.id.content),
                                    "Событие '${name}' удалено",
                                    Snackbar.LENGTH_LONG
                                )
                                snackBar.setAction("Отмена") {
                                }
                                snackBar.setOnHideByTimeout {
                                    viewModel.deleteEvent(event)
                                }
                                snackBar.show()
                            }
                        })
                        val fm: FragmentManager = requireActivity().supportFragmentManager
                        dialog.show(fm, dialog.tag)
                    }
                }

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


        }

        binding.timeIntervalSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                var selected: TimeInterval = TimeInterval.All
                when (position) {
                    0 -> {
                        selected = TimeInterval.Today
                    }
                    1 -> {
                        selected = TimeInterval.Week
                    }
                    2 -> {
                        selected = TimeInterval.Month
                    }
                    3 -> {
                        selected = TimeInterval.All
                    }
                    4 -> {
                        selected = TimeInterval.Selected(startIntervalCalendar.timeInMillis, endIntervalCalendar.timeInMillis)
                    }
                }

                viewModel.saveTimeIntervalState(selected)
                viewModel.runSearch()
            }

            override fun onNothingSelected(parent: AdapterView<*>){}
        }

        val onStartDateSelected = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            startIntervalCalendar.set(Calendar.YEAR, year)
            startIntervalCalendar.set(Calendar.MONTH, month)
            startIntervalCalendar.set(Calendar.DAY_OF_MONTH, day)
            viewModel.saveTimeIntervalState(TimeInterval.Selected(startIntervalCalendar.timeInMillis, endIntervalCalendar.timeInMillis))
            viewModel.runSearch()
        }

        val onEndDateSelected = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            endIntervalCalendar.set(Calendar.YEAR, year)
            endIntervalCalendar.set(Calendar.MONTH, month)
            endIntervalCalendar.set(Calendar.DAY_OF_MONTH, day)
            viewModel.saveTimeIntervalState(TimeInterval.Selected(startIntervalCalendar.timeInMillis, endIntervalCalendar.timeInMillis))
            viewModel.runSearch()
        }

        binding.timeIntervalStartButton.setOnClickListener {
            DatePickerDialog(requireContext(),onStartDateSelected, startIntervalCalendar.get(Calendar.YEAR), startIntervalCalendar.get(
                Calendar.MONTH), startIntervalCalendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        binding.timeIntervalEndButton.setOnClickListener {
            DatePickerDialog(requireContext(), onEndDateSelected, endIntervalCalendar.get(Calendar.YEAR), endIntervalCalendar.get(
                Calendar.MONTH), endIntervalCalendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        return binding.root
    }
}