package com.skyfolk.quantoflife.ui.feeds

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.FeedsFragmentComposeBinding
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.ui.theme.ComposeFlowTestTheme
import com.skyfolk.quantoflife.ui.theme.Typography
import com.skyfolk.quantoflife.utils.fromPositionToTimeInterval
import com.skyfolk.quantoflife.utils.toDateWithoutHourAndMinutes
import com.skyfolk.quantoflife.utils.toPosition
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class FeedsComposeFragment : Fragment() {
    private val viewModel: FeedsViewModel by viewModel()
    private lateinit var binding: FeedsFragmentComposeBinding

    // private val dateTimeRepository: IDateTimeRepository by inject()
    private val startIntervalCalendar = Calendar.getInstance() //dateTimeRepository.getCalendar()
    private val endIntervalCalendar = Calendar.getInstance() // = dateTimeRepository.getCalendar()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FeedsFragmentComposeBinding.inflate(inflater, container, false)
            .apply {
                composeView.setContent {
                    // You're in Compose world!
                    val state by viewModel.state.observeAsState() // TODO Remember
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


                                    // A surface container using the 'background' color from the theme
                                    //Surface(color = MaterialTheme.colors.background) {
                                    TotalValues(
                                        categoryArray,
                                        categoryValue, state.totalFound,
                                        state.totalStarFound, state.listOfEvents.size
                                    )
                                }
                            }

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
                                            fromPositionToTimeInterval(position, start, end))
                                    }
                                )

                                if (interval is TimeInterval.Selected) {
                                    HalfHorizontalLayout(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 5.dp)
                                    ) {
                                        TimeSelectLayout(
                                            time = interval.start,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
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
                                        TimeSelectLayout(
                                            time = interval.end,
                                            horizontalArrangement = Arrangement.End
                                        ) {
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
                                    }
                                }
                            }
                        }

                    }
                }
            }

        viewModel.setSelectedEventFilter(null)
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

@Composable
fun FilterBlock(
    listOfQuantNames: List<String>,
    selectedQuantPosition: Int?,
    onQuantFilterClick: (Int) -> Unit,
    listOfTimeInterval: List<String>,
    selectedTimeIntervalPosition: Int,
    onTimeIntervalFilterClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
        ) {
            SmallSubtitle(text = "Фильтры.")
            DropdownSpinner(content = listOfQuantNames, selectedItemIndex = selectedQuantPosition ?: 0) {
                onQuantFilterClick(it)
            }
            DropdownSpinner(content = listOfTimeInterval, selectedItemIndex = selectedTimeIntervalPosition) {
                onTimeIntervalFilterClick(it)
            }
            SeparatorLine()
        }
    }
}

@Composable
fun DropdownSpinner(content: List<String>, selectedItemIndex: Int, onItemSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(selectedItemIndex) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expanded = true }),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = content[selectedIndex],
                style = Typography.body2,
            )
            Image(
                painter = painterResource(R.drawable.ic_feed),
                contentDescription = "",
                Modifier.size(size = 20.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            content.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    expanded = false
                    onItemSelect(index)
                }) {
                    Text(text = content[index])
                }
            }
        }
    }
}

@Composable
fun TimeSelectLayout(time: Long, horizontalArrangement: Arrangement.Horizontal, onTimeSelectClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Text(
            text = time.toDateWithoutHourAndMinutes(),
            style = Typography.body2,
        )
        Image(
            painter = painterResource(R.drawable.quant_date),
            contentDescription = "",
            Modifier.size(size = 30.dp).clickable(onClick = { onTimeSelectClick() })
        )
    }
}

@Composable
fun TotalValues(
    descriptionsList: List<String>,
    valuesList: List<Double>,
    totalFound: Double,
    totalStar: Int,
    totalEventFound: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        SmallSubtitle(text = "Итого за период найдено $totalEventFound событий.")
        TotalValue(description = descriptionsList[0], value = valuesList[0])
        TotalValue(description = descriptionsList[1], value = valuesList[1])
        TotalValue(description = descriptionsList[2], value = valuesList[2])
        TotalValue(description = "звезд", value = totalStar.toDouble(), valueFormatAfterDot = 0)
        TotalValue(description = "", value = totalFound, style = Typography.subtitle2)
        SeparatorLine()
    }
}

@Composable
fun SeparatorLine() {
    Divider(
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .padding(top = 5.dp),
        color = Color.Black,
        thickness = 1.dp
    )
}

@Composable
fun SmallSubtitle(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        text = text,
        style = Typography.subtitle1,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TotalValue(
    description: String,
    value: Double, valueFormatAfterDot: Int = 1,
    style: TextStyle = LocalTextStyle.current
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Всего ${description.toLowerCase(Locale.ROOT)} :",
            textAlign = TextAlign.Left,
            style = style
        )
        Text(
            text = String.format("%.${valueFormatAfterDot}f", value),
            textAlign = TextAlign.Right,
            style = style
        )
    }
}

@Composable
fun HalfHorizontalLayout(modifier: Modifier, children: @Composable () -> Unit) {
    Layout(children, modifier) { measurables, constraints ->
        layout(constraints.maxWidth, constraints.maxHeight) {
            val halfWidth = constraints.maxWidth / 2
            val childConstraints = constraints.copy(
                minWidth = minOf(constraints.minWidth, halfWidth),
                maxWidth = halfWidth
            )
            require(measurables.size == 2)
            measurables[0].measure(childConstraints).place(0, 0)
            measurables[1].measure(childConstraints).place(halfWidth, 0)
        }
    }
}

@Preview(showBackground = false)
@Composable
fun DefaultPreview() {
    ComposeFlowTestTheme {
        HalfHorizontalLayout(Modifier.fillMaxWidth().padding(start = 5.dp, end = 5.dp)) {
            TimeSelectLayout(time = 1620729831235, horizontalArrangement = Arrangement.Start) {}
            TimeSelectLayout(time = 1620729831235, horizontalArrangement = Arrangement.End) {}
        }
//        HalfHorizontalLayout(Modifier.fillMaxSize()) {
//            Box(modifier = Modifier.background(Color(Color.Cyan.value)))
//            Box(modifier = Modifier.background(Color(Color.Red.value)))
//        }
//        CircularProgressIndicator()
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 5.dp)
//        ) {
//            TotalValues(
//                arrayListOf("Physical", "Emotion", "Evolution"),
//                arrayListOf(3.0, 1.0, 9.0),
//                9848.0,
//                52.0,
//                104
//            )
//            FilterBlock()
//        }
    }
}