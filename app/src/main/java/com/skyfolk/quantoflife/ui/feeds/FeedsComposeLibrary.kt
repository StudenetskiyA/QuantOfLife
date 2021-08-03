package com.skyfolk.quantoflife.ui.feeds

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.entity.EventDisplayable
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.ui.theme.Orange
import com.skyfolk.quantoflife.ui.theme.Typography
import com.skyfolk.quantoflife.utils.toDate
import com.skyfolk.quantoflife.utils.toDateWithoutHourAndMinutes
import java.util.*

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

@Composable
fun TotalValue(
    description: String,
    value: Double?, valueFormatAfterDot: Int = 1,
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
            text = if (value!=null) String.format("%.${valueFormatAfterDot}f", value) else "",
            textAlign = TextAlign.Right,
            style = style
        )
    }
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
fun TotalValues(state: FeedsFragmentState) {
    val descriptionsList = getCategoryArrayNames(state)
    val valuesList = when (state) {
        is FeedsFragmentState.EventsListLoading -> arrayOfNulls<Double?>(descriptionsList.size).toList()
        is FeedsFragmentState.LoadingEventsListCompleted -> getCategoryArrayValues(state)
    }
    val subtitle = when (state) {
        is FeedsFragmentState.EventsListLoading -> "...."
        is FeedsFragmentState.LoadingEventsListCompleted -> "Итого за период найдено ${state.listOfEvents.size} событий."
    }
    val totalStarFound: Double? = when (state) {
        is FeedsFragmentState.EventsListLoading -> null
        is FeedsFragmentState.LoadingEventsListCompleted -> state.totalStarFound.toDouble()
    }
    val totalFound: Double? = when (state) {
        is FeedsFragmentState.EventsListLoading -> null
        is FeedsFragmentState.LoadingEventsListCompleted -> state.totalFound
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        SmallSubtitle(text = subtitle)
        TotalValue(description = descriptionsList[0], value = valuesList[0])
        TotalValue(description = descriptionsList[1], value = valuesList[1])
        TotalValue(description = descriptionsList[2], value = valuesList[2])
        TotalValue(
            description = "звезд",
            value = totalStarFound,
            valueFormatAfterDot = 0
        )
        TotalValue(description = "", value = totalFound, style = Typography.subtitle2)
        SeparatorLine()
    }
}

@Composable
fun TimeSelectLayout(
    time: Long,
    horizontalArrangement: Arrangement.Horizontal,
    onTimeSelectClick: () -> Unit
) {
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
            Modifier
                .size(size = 30.dp)
                .clickable(onClick = { onTimeSelectClick() })
        )
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
                painter = painterResource(R.drawable.ic_dropdown),
                contentDescription = "",
                Modifier.size(size = 20.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
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
            DropdownSpinner(
                content = listOfQuantNames,
                selectedItemIndex = selectedQuantPosition ?: 0
            ) {
                onQuantFilterClick(it)
            }
            DropdownSpinner(
                content = listOfTimeInterval,
                selectedItemIndex = selectedTimeIntervalPosition
            ) {
                onTimeIntervalFilterClick(it)
            }
            SeparatorLine()
        }
    }
}

@Composable
fun EventItem(event: EventDisplayable, modifier: Modifier) {
    Card(
        backgroundColor = Orange,
        modifier = modifier
            .height(70.dp)
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Row(modifier = modifier) {
            var imageResource = LocalContext.current.resources.getIdentifier(
                event.icon,
                "drawable",
                LocalContext.current.packageName
            )
            if (imageResource == 0) {
                imageResource = LocalContext.current.resources.getIdentifier(
                    "quant_default",
                    "drawable",
                    LocalContext.current.packageName
                )
            }
            Image(
                painter = painterResource(imageResource),
                contentDescription = "event_icon",
                modifier = modifier
                    .height(50.dp)
                    .width(50.dp)
            )
            Column(
                modifier = modifier
                    .padding(horizontal = 5.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = modifier.fillMaxWidth()
                ) {
                    Text(event.name)
                    Text(event.date.toDate())
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = modifier.fillMaxWidth()
                ) {
                    when {
                        ((event.bonuses != null) && (event.value != null)) -> {
                            RatingBar(
                                rating = event.value.toFloat(),
                                color = Color.Cyan,
                                modifier = modifier.height(20.dp)
                            )
                        }
                        ((event.bonuses == null) && (event.value != null)) -> {
                            Text(event.value.toString())
                        }
                    }
                }
                Text(event.note)
            }
        }
    }
}

@Composable
fun EventsList(modifier: Modifier, events: List<EventDisplayable>, onItemClick: (String) -> Unit) {
    LazyColumn(modifier = modifier) {
        events.map {
            item {
                EventItem(
                    event = it,
                    modifier = Modifier.clickable {
                        onItemClick(it.id)
                    })
            }
        }
    }
}

@Composable
fun SelectedTimeInterval(
    context: Context,
    setTimeInterval: (TimeInterval.Selected) -> Unit,
    intervalStart: Calendar,
    intervalEnd: Calendar
) {
    val onStartDateSelected =
        DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            intervalStart.set(Calendar.YEAR, year)
            intervalStart.set(Calendar.MONTH, month)
            intervalStart.set(Calendar.DAY_OF_MONTH, day)
            setTimeInterval(
                TimeInterval.Selected(
                    intervalStart.timeInMillis,
                    intervalEnd.timeInMillis
                )
            )
        }

    val onEndDateSelected =
        DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            intervalEnd.set(Calendar.YEAR, year)
            intervalEnd.set(Calendar.MONTH, month)
            intervalEnd.set(Calendar.DAY_OF_MONTH, day)
            setTimeInterval(
                TimeInterval.Selected(
                    intervalStart.timeInMillis,
                    intervalEnd.timeInMillis
                )
            )
        }

    HalfHorizontalLayout(
        Modifier
            .fillMaxWidth()
            .padding(start = 5.dp)
    ) {
        TimeSelectLayout(
            time = intervalStart.timeInMillis,
            horizontalArrangement = Arrangement.Start
        ) {
            DatePickerDialog(
                context,
                onStartDateSelected,
                intervalStart.get(Calendar.YEAR),
                intervalStart.get(
                    Calendar.MONTH
                ),
                intervalStart.get(Calendar.DAY_OF_MONTH)
            )
                .show()
        }
        TimeSelectLayout(
            time = intervalEnd.timeInMillis,
            horizontalArrangement = Arrangement.End
        ) {
            DatePickerDialog(
                context,
                onEndDateSelected,
                intervalEnd.get(Calendar.YEAR),
                intervalEnd.get(
                    Calendar.MONTH
                ),
                intervalEnd.get(Calendar.DAY_OF_MONTH)
            )
                .show()
        }
    }
}

private fun getCategoryArrayNames(state: FeedsFragmentState): List<String> {
    return listOf(
        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Physical }?.second
            ?: "",
        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Emotion }?.second
            ?: "",
        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Evolution }?.second
            ?: "",
        state.quantCategoryNames.firstOrNull { it.first == QuantCategory.Other }?.second
            ?: ""
    )
}

private fun getCategoryArrayValues(state: FeedsFragmentState.LoadingEventsListCompleted): List<Double> {
    return listOf(
        state.totalPhysicalFound,
        state.totalEmotionalFound,
        state.totalEvolutionFound
    )
}