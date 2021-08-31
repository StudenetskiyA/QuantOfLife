package com.skyfolk.quantoflife.ui.now

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skyfolk.quantoflife.DateTimeRepository
import com.skyfolk.quantoflife.IDateTimeRepository
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.CreateEventDialogBinding
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.utils.toDate
import org.koin.android.ext.android.inject
import java.util.*

class CreateEventDialogFragment(val quant: QuantBase, private val existEvent: EventBase? = null) : BottomSheetDialogFragment() {
    private var dialogListener: DialogListener? = null

    private lateinit var binding: CreateEventDialogBinding

    private val dateTimeRepository: IDateTimeRepository by inject()
    private val calendar = dateTimeRepository.getCalendar()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateEventDialogBinding.inflate(inflater, container, false)
        val imageResource = requireContext().resources.getIdentifier(quant.icon, "drawable", requireContext().packageName)
        if (imageResource !=0 ) {
            binding.quantImage.setImageResource(imageResource)
        } else {
            binding.quantImage.setImageResource(requireContext().resources.getIdentifier("quant_default", "drawable", requireContext().packageName))
        }
        binding.eventName.text = quant.name
        binding.eventDescription.text = quant.description

        binding.eventDateChoiceButton.setOnClickListener {
            DatePickerDialog(requireContext(),onDateSelected, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        existEvent?.let {
            binding.buttonDelete.visibility = View.VISIBLE
            binding.eventNote.setText(it.note)
            binding.eventDate.text = it.date.toDate()
            calendar.timeInMillis = it.date
            when (it) {
                is EventBase.EventRated -> {
                    binding.eventRating.rating = it.rate.toFloat()
                }
                is EventBase.EventMeasure -> {
                    binding.eventRatingNumeric.setText(it.value.toString())
                }
                else -> {}
            }
        }

        when (quant) {
            is QuantBase.QuantNote -> {
                binding.eventRating.visibility = View.GONE
                binding.eventRatingNumeric.visibility = View.GONE
            }
            is QuantBase.QuantRated -> {
                binding.eventRating.visibility = View.VISIBLE
                binding.eventRatingNumeric.visibility = View.GONE
            }
//            is QuantBase.QuantRatedFact -> {
//                binding.eventRating.visibility = View.GONE
//            }
            is QuantBase.QuantMeasure -> {
                binding.eventRating.visibility = View.GONE
                binding.eventRatingNumeric.visibility = View.VISIBLE
            }
        }

        binding.eventNote.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputManager: InputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                    binding.eventNote.applicationWindowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                binding.eventNote.clearFocus()
            }
            false
        }

        binding.buttonOk.setOnClickListener {
            if (quant is QuantBase.QuantMeasure) {
                if (binding.eventRatingNumeric.text.toString().isEmpty()) {
                    Toast.makeText(context, "Не заполненны числовые поля", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
            }
            dialogListener?.onConfirm(
                quant.toEvent(
                    existEvent?.id,
                    when (quant) {
                        is QuantBase.QuantRated -> binding.eventRating.rating.toDouble()
                        is QuantBase.QuantMeasure -> binding.eventRatingNumeric.text.toString().toDouble()
                        is QuantBase.QuantNote -> (-1).toDouble()
                    }, calendar.timeInMillis,
                    binding.eventNote.text.toString()
                ), quant.name
            )
            dismiss()
        }

        binding.buttonDelete.setOnClickListener {
            if (existEvent != null) {
                dialogListener?.onDelete(existEvent, quant.name)
            }
            dismiss()
        }

        return binding.root
    }

    private val onDateSelected = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        TimePickerDialog(requireContext(),onTimeSelected, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            .show()
    }
    private val onTimeSelected = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)

        binding.eventDate.text = calendar.timeInMillis.toDate()
    }

    fun setDialogListener(listener: DialogListener) {
        dialogListener = listener
    }

    interface DialogListener {
        fun onConfirm(event: EventBase, name: String)
        fun onDecline()
        fun onDelete(event: EventBase, name: String)
    }
}