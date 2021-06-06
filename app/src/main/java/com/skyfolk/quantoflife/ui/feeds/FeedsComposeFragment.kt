package com.skyfolk.quantoflife.ui.feeds

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.FeedsFragmentComposeBinding
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.EventDisplayable
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.ui.now.CreateEventDialogFragment
import com.skyfolk.quantoflife.ui.theme.ComposeFlowTestTheme
import com.skyfolk.quantoflife.ui.theme.Orange
import com.skyfolk.quantoflife.utils.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class FeedsComposeFragment : Fragment() {
    private val viewModel: FeedsViewModel by viewModel()
    private lateinit var binding: FeedsFragmentComposeBinding

//    // private val dateTimeRepository: IDateTimeRepository by inject()
//    private val startIntervalCalendar = Calendar.getInstance() //dateTimeRepository.getCalendar()
//    private val endIntervalCalendar = Calendar.getInstance() // = dateTimeRepository.getCalendar()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FeedsFragmentComposeBinding.inflate(inflater, container, false)
            .apply {
                composeView.setContent {
                    val state by viewModel.state.observeAsState() // TODO Remember
                    val startIntervalCalendar = remember {
                        val calendar = Calendar.getInstance()
                        calendar
                    }
                    val endIntervalCalendar = remember {
                        val calendar = Calendar.getInstance()
                        calendar
                    }

                    ComposeFlowTestTheme {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            // State type
                            when (state) {
                                is FeedsFragmentState.EventsListLoading -> {
                                    // TODO Not fit to screen
                                    CircularProgressIndicator()
                                }
                                is FeedsFragmentState.LoadingEventsListCompleted -> {
                                    val state =
                                        state as FeedsFragmentState.LoadingEventsListCompleted
                                    val categoryArray = listOf(
                                        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Physical }?.second
                                            ?: "",
                                        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Emotion }?.second
                                            ?: "",
                                        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Evolution }?.second
                                            ?: "",
                                        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Other }?.second
                                            ?: ""
                                    )
                                    val categoryValue = listOf(
                                        state.totalPhysicalFound,
                                        state.totalEmotionalFound,
                                        state.totalEvolutionFound
                                    )

                                    LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                                        state.listOfEvents.reversed().map {
                                            item {
                                                EventItem(
                                                    event = it,
                                                    modifier = Modifier.clickable {
                                                        viewModel.editEvent(it.id)
                                                    })
                                            }
                                        }
                                    }

                                    SeparatorLine()

                                    TotalValues(
                                        categoryArray,
                                        categoryValue, state.totalFound,
                                        state.totalStarFound, state.listOfEvents.size
                                    )
                                }
                            }

                            // Common state
                            state?.let { state ->
                                val listOfQuantName =
                                    state.listOfQuants.map { it.name }.toMutableList()
                                listOfQuantName.add(0, "Все события")

                                val listOfTimeInterval =
                                    resources.getStringArray(R.array.time_interval)

                                val interval = state.selectedTimeInterval
                                val selectedQuantPosition = state.selectedEventFilter?.let {
                                    val filterName = viewModel.getQuantNameById(it)
                                    listOfQuantName.indexOf(filterName)
                                }

                                FilterBlock(
                                    listOfQuantNames = listOfQuantName,
                                    selectedQuantPosition = selectedQuantPosition,
                                    onQuantFilterClick = { position ->
                                        if (position == 0) {
                                            viewModel.setSelectedEventFilter(null)
                                        } else {
                                            viewModel.setSelectedEventFilter(
                                                viewModel.getQuantIdByName(
                                                    listOfQuantName[position]
                                                )
                                            )
                                        }
                                    },
                                    listOfTimeInterval = listOfTimeInterval.toList(),
                                    selectedTimeIntervalPosition = state.selectedTimeInterval.toPosition(),
                                    onTimeIntervalFilterClick = { position ->
                                        val start = startIntervalCalendar.timeInMillis
                                        val end = endIntervalCalendar.timeInMillis

                                        viewModel.setTimeIntervalState(
                                            fromPositionToTimeInterval(position, start, end)
                                        )
                                    }
                                )

                                if (interval is TimeInterval.Selected) {
                                    SelectedTimeInterval(
                                        LocalContext.current,
                                        { viewModel.setTimeIntervalState(it) },
                                        Calendar.getInstance().timeInMillis(interval.start),
                                        Calendar.getInstance().timeInMillis(interval.end)
                                    )
                                }
                            }
                        }
                    }
                }
            }

        viewModel.singleLifeEvent.observe(
            viewLifecycleOwner,
            { event ->
                when (event) {
                    is FeedsFragmentSingleLifeEvent.ShowEditEventDialog -> {
                        val dialog = CreateEventDialogFragment(event.quant, event.event)
                        dialog.setDialogListener(object :
                            CreateEventDialogFragment.DialogListener {
                            override fun onConfirm(event: EventBase, name: String) {
                                val snackBar = com.google.android.material.snackbar.Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
                                    "Событие '${name}' изменено",
                                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
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
                                val snackBar = com.google.android.material.snackbar.Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
                                    "Событие '${name}' удалено",
                                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
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

        // TODO Init viewModel
        viewModel.setSelectedEventFilter(viewModel.getSelectedEventFilter())
        return binding.root
    }
}

@Preview(showBackground = false)
@Composable
fun DefaultPreview() {
    ComposeFlowTestTheme {
    }
}