package com.skyfolk.quantoflife.ui.feeds

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.FeedsFragmentComposeBinding
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.ui.now.CreateEventDialogFragment
import com.skyfolk.quantoflife.ui.statistic.NavigateToFeedEvent
import com.skyfolk.quantoflife.ui.theme.Colors
import com.skyfolk.quantoflife.ui.theme.ComposeFlowTestTheme
import com.skyfolk.quantoflife.utils.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class FeedsComposeFragment : Fragment() {
    private val viewModel: FeedsViewModel by viewModel()
    private lateinit var binding: FeedsFragmentComposeBinding

    override fun onResume() {
        super.onResume()

        arguments?.let {
            val start = it.getLong(NavigateToFeedEvent.START_DATE_KEY)
            val end = it.getLong(NavigateToFeedEvent.END_DATE_KEY)


            if (start != 0L && end != 0L ) {
                QLog.d("skyfolk-graph","event from graph ${start.toDate()} to ${end.toDate()}")

                viewModel.setTimeIntervalState(
                    TimeInterval.Selected(start, end)
                )
            } else {
                viewModel.setSelectedEventFilter(null, true)
            }
        }
    }

    @ExperimentalComposeUiApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FeedsFragmentComposeBinding.inflate(inflater, container, false)
            .apply {
                composeView.setContent {

                    val state by viewModel.state.collectAsState()
                    val startIntervalCalendar = remember { viewModel.getDefaultCalendar() }
                    val endIntervalCalendar = remember { viewModel.getDefaultCalendar() }

                    ComposeFlowTestTheme {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(vertical = 5.dp)
                        ) {
                            when (state) {
                                is FeedsFragmentState.EventsListLoading -> {
                                    binding.progress.visibility = View.VISIBLE
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(2f)
                                            .fillMaxWidth()
                                    ) {
                                        //CircularProgressIndicator()
                                    }
                                }
                                is FeedsFragmentState.LoadingEventsListCompleted -> {
                                    binding.progress.visibility = View.GONE
                                    val state =
                                        state as FeedsFragmentState.LoadingEventsListCompleted

                                    EventsList(
                                        modifier = Modifier.weight(2f),
                                        events = state.listOfEvents.reversed()
                                    ) { id ->
                                        viewModel.editEvent(id)
                                    }


                                    SeparatorLine()
                                }
                            }

                            // Common state
                            state.let { state ->
                                val listOfQuantName =
                                    state.listOfQuants.map { it.name }.toMutableList()
                                listOfQuantName.add(0, "Все события")

                                val selectedQuantPosition = state.selectedEventFilter?.let {
                                    if (listOfQuantName.indexOf(it) != -1) listOfQuantName.indexOf(it) else 0
                                }

                                TotalValues(state, modifier = Modifier)

                                FilterBlock(
                                    listOfQuantNames = listOfQuantName,
                                    selectedQuantPosition = selectedQuantPosition,
                                    onQuantFilterClick = { position ->
                                        if (position == 0) {
                                            viewModel.setSelectedEventFilter(null)
                                        } else {
                                            viewModel.setSelectedEventFilter(listOfQuantName[position])
                                        }
                                    },
                                    listOfTimeInterval = resources.getStringArray(R.array.time_interval)
                                        .toList(),
                                    selectedTimeIntervalPosition = state.selectedTimeInterval.toPosition(),
                                    selectedTextFilter = state.selectedTextFilter,
                                    onTimeIntervalFilterClick = { position ->
                                        val start = startIntervalCalendar.timeInMillis
                                        val end = endIntervalCalendar.timeInMillis

                                        viewModel.setTimeIntervalState(
                                            fromPositionToTimeInterval(position, start, end)
                                        )
                                    },
                                    onTextSearchEnter = {
                                        viewModel.setSearchText(it)
                                    },
                                    modifier = Modifier
                                )

                                (state.selectedTimeInterval as? TimeInterval.Selected)?.let { interval ->
                                    SelectedTimeInterval(
                                        LocalContext.current,
                                        {
                                            QLog.d("skyfolk-graph","set ${it.start} to ${it.end}")
                                            viewModel.setTimeIntervalState(it) },
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

        return binding.root
    }
}