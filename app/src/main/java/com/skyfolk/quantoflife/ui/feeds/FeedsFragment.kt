package com.skyfolk.quantoflife.ui.feeds

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.skyfolk.quantoflife.databinding.FeedsFragmentBinding
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.ui.now.CreateEventDialogFragment
import com.skyfolk.quantoflife.utils.setOnHideByTimeout
import com.skyfolk.quantoflife.utils.toDateWithoutHourAndMinutes
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

@Deprecated("Устаревшая версия экрана, вместо неё используйте FeedsComposeFragment",
    ReplaceWith("FeedsComposeFragment"),
    DeprecationLevel.WARNING)
class FeedsFragment : Fragment() {
    private val viewModel: FeedsViewModel by viewModel()
    private lateinit var binding: FeedsFragmentBinding

   // private val dateTimeRepository: IDateTimeRepository by inject()
    private val startIntervalCalendar = Calendar.getInstance() //dateTimeRepository.getCalendar()
    private val endIntervalCalendar =  Calendar.getInstance() // = dateTimeRepository.getCalendar()

    // Иначе дурацкие спиннеры лишние раз тригерятся
    private var selectedEventFilterName: String? = null
    private var selectedTimeInterval: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FeedsFragmentBinding.inflate(inflater, container, false)

        val listOfEvents: RecyclerView = binding.listOfEvents
        listOfEvents.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)

        lifecycleScope.launch {
            viewModel.singleLifeEvent.observe(
                viewLifecycleOwner,
                { event ->
                    when (event) {
                        is FeedsFragmentSingleLifeEvent.ShowEditEventDialog -> {
                            val dialog = CreateEventDialogFragment(event.quant, event.event)
                            dialog.setDialogListener(object :
                                CreateEventDialogFragment.DialogListener {
                                override fun onConfirm(event: EventBase, name: String) {
                                    val snackBar = Snackbar.make(
                                        requireActivity().findViewById(android.R.id.content),
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
                                        requireActivity().findViewById(android.R.id.content),
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
                            dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                        }
                    }
                }
            )

            viewModel.state.onEach { state: FeedsFragmentState ->
                    // Descriptions
                    val categoryArray = mutableListOf(
                        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Physical }?.second ?: "",
                        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Emotion }?.second ?: "",
                        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Evolution }?.second ?: "",
                        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Other }?.second ?: ""
                    )
                    binding.physicalDescription.text = "Всего ${categoryArray[0]} :"
                    binding.emotionalDescription.text = "Всего ${categoryArray[1]} :"
                    binding.evolutionDescription.text = "Всего ${categoryArray[2]} :"

                    // Selected time interval
                    val timeInterval = state.selectedTimeInterval

                    when (timeInterval) {
                        is TimeInterval.Today -> {
                            //selectedTimeInterval = binding.timeIntervalSpinner.getItemAtPosition(0).toString()
                            binding.timeIntervalSpinner.setSelection(0, false)
                        }
                        is TimeInterval.Week -> {
                            //selectedTimeInterval = binding.timeIntervalSpinner.getItemAtPosition(1).toString()
                            binding.timeIntervalSpinner.setSelection(1, false)
                        }
                        is TimeInterval.Month -> {
                            //selectedTimeInterval = binding.timeIntervalSpinner.getItemAtPosition(2).toString()
                            binding.timeIntervalSpinner.setSelection(2, false)
                        }
                        is TimeInterval.Selected -> {
                           // selectedTimeInterval = binding.timeIntervalSpinner.getItemAtPosition(4).toString()
                            binding.timeIntervalSpinner.setSelection(4, false)
                            binding.selectedTimeIntervalStartLabel.text =
                                timeInterval.start.toDateWithoutHourAndMinutes()
                            binding.selectedTimeIntervalEndLabel.text =
                                timeInterval.end.toDateWithoutHourAndMinutes()
                            startIntervalCalendar.timeInMillis = timeInterval.start
                            endIntervalCalendar.timeInMillis = timeInterval.end
                        }
                        else -> {
                            //selectedTimeInterval = binding.timeIntervalSpinner.getItemAtPosition(3).toString()
                            binding.timeIntervalSpinner.setSelection(3, false)
                        }
                    }
                    binding.selectedTimeIntervalLayout.visibility =
                        if (timeInterval is TimeInterval.Selected) View.VISIBLE else View.INVISIBLE

                    // List of quants for spinner
                    val listOfQuantName = state.listOfQuants.map { it.name }.toMutableList()
                    listOfQuantName.add(0, "Все события")
                    val quantsSpinnerAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        listOfQuantName
                    )
                    quantsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinner.adapter = quantsSpinnerAdapter
                    state.selectedEventFilter?.let {
                        val index = listOfQuantName.indexOf(it)
                        selectedEventFilterName = it
                        binding.spinner.setSelection(index, false)
                    } ?: run {
                        selectedEventFilterName = listOfQuantName[0]
                        binding.spinner.setSelection(0, false)
                    }

                    // State type
                    when (state) {
                        is FeedsFragmentState.EventsListLoading -> {
                            binding.eventListLoadingProgress.visibility = View.VISIBLE
                            binding.itemsNotFound.visibility = View.GONE
                            binding.listOfEvents.visibility = View.GONE
                        }
                        is FeedsFragmentState.LoadingEventsListCompleted -> {
                            val eventsListAdapter = EventListDataAdapter(
                                state.listOfEvents,
                                state.quantCategoryNames
                            ) { eventId ->
                                viewModel.editEvent(eventId)
                            }

                            binding.eventListLoadingProgress.visibility = View.GONE
                            if (eventsListAdapter.itemCount == 0) {
                                binding.itemsNotFound.visibility = View.VISIBLE
                                binding.listOfEvents.visibility = View.GONE
                            } else {
                                binding.itemsNotFound.visibility = View.GONE
                                binding.listOfEvents.visibility = View.VISIBLE
                            }
                            binding.totalDescription.text =
                                "Итого за период найдено ${eventsListAdapter.itemCount} событий."
                            listOfEvents.adapter = eventsListAdapter

                            binding.physicalValue.text =
                                String.format("%.1f", state.totalPhysicalFound)
                            binding.emotionalValue.text =
                                String.format("%.1f", state.totalEmotionalFound)
                            binding.evolutionValue.text =
                                String.format("%.1f", state.totalEvolutionFound)
                            binding.totalValue.text = String.format("%.1f", state.totalFound)
                            binding.starValue.text = state.totalStarFound.toString()
                        }
                    }
                }
        }

        binding.timeIntervalSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (selectedTimeInterval != parent.getItemAtPosition(position).toString()) {
                        selectedTimeInterval = parent.getItemAtPosition(position).toString()
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
                                selected = TimeInterval.Selected(
                                    startIntervalCalendar.timeInMillis,
                                    endIntervalCalendar.timeInMillis
                                )
                            }
                        }

                        Log.d(
                            "skyfolk-spinner",
                            "timeIntervalSpinner.onItemSelectedListener = ${selected.javaClass.name}"
                        )

                        viewModel.setTimeIntervalState(selected)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.timeIntervalStartButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                onStartDateSelected,
                startIntervalCalendar.get(Calendar.YEAR),
                startIntervalCalendar.get(
                    Calendar.MONTH
                ),
                startIntervalCalendar.get(Calendar.DAY_OF_MONTH)
            )
                .show()
        }

        binding.timeIntervalEndButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                onEndDateSelected,
                endIntervalCalendar.get(Calendar.YEAR),
                endIntervalCalendar.get(
                    Calendar.MONTH
                ),
                endIntervalCalendar.get(Calendar.DAY_OF_MONTH)
            )
                .show()
        }

        binding.spinner.onItemSelectedListener =
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
                                    parent.getItemAtPosition(position).toString()
                            )
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        return binding.root
    }

    private val onStartDateSelected =
        DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            startIntervalCalendar.set(Calendar.YEAR, year)
            startIntervalCalendar.set(Calendar.MONTH, month)
            startIntervalCalendar.set(Calendar.DAY_OF_MONTH, day)
            viewModel.setTimeIntervalState(
                TimeInterval.Selected(
                    startIntervalCalendar.timeInMillis,
                    endIntervalCalendar.timeInMillis
                )
            )
        }

    private val onEndDateSelected =
        DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            endIntervalCalendar.set(Calendar.YEAR, year)
            endIntervalCalendar.set(Calendar.MONTH, month)
            endIntervalCalendar.set(Calendar.DAY_OF_MONTH, day)
            viewModel.setTimeIntervalState(
                TimeInterval.Selected(
                    startIntervalCalendar.timeInMillis,
                    endIntervalCalendar.timeInMillis
                )
            )
        }
}

