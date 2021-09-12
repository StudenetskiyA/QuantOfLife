package com.skyfolk.quantoflife.ui.feeds

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.FeedsFragmentComposeBinding
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.ui.now.CreateEventDialogFragment
import com.skyfolk.quantoflife.ui.theme.ComposeFlowTestTheme
import com.skyfolk.quantoflife.utils.fromPositionToTimeInterval
import com.skyfolk.quantoflife.utils.setOnHideByTimeout
import com.skyfolk.quantoflife.utils.timeInMillis
import com.skyfolk.quantoflife.utils.toPosition
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class FeedsComposeFragment : Fragment() {
    private val viewModel: FeedsViewModel by viewModel()
    private lateinit var binding: FeedsFragmentComposeBinding

    override fun onResume() {
        super.onResume()
        Log.d("skyfolk-timer", "onResume: ${System.currentTimeMillis()}" )
        viewModel.setSelectedEventFilter(null, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("skyfolk-timer", "onCreateView: ${System.currentTimeMillis()}" )
        binding = FeedsFragmentComposeBinding.inflate(inflater, container, false)
            .apply {
                composeView.setContent {
                    Log.d("skyfolk-timer", "topUpdateState: ${System.currentTimeMillis()}" )

                    val state by viewModel.state.collectAsState()
                    val startIntervalCalendar = remember { viewModel.getDefaultCalendar() }
                    val endIntervalCalendar = remember { viewModel.getDefaultCalendar() }
                   // var count = remember { mutableStateOf(0) }

                    ComposeFlowTestTheme {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            when (state) {
                                is FeedsFragmentState.EventsListLoading -> {
                                    binding.progress.visibility = View.VISIBLE
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxHeight(0.6f)
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
                                        modifier = Modifier.fillMaxHeight(0.6f),
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

                                TotalValues(state)

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
                                    onTimeIntervalFilterClick = { position ->
                                        val start = startIntervalCalendar.timeInMillis
                                        val end = endIntervalCalendar.timeInMillis

                                        viewModel.setTimeIntervalState(
                                            fromPositionToTimeInterval(position, start, end)
                                        )
                                    }
                                )

                                (state.selectedTimeInterval as? TimeInterval.Selected)?.let { interval ->
                                    SelectedTimeInterval(
                                        LocalContext.current,
                                        { viewModel.setTimeIntervalState(it) },
                                        Calendar.getInstance().timeInMillis(interval.start),
                                        Calendar.getInstance().timeInMillis(interval.end)
                                    )
                                }
                                Log.d("skyfolk-timer", "endUpdateState: ${System.currentTimeMillis()}" )

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