package com.skyfolk.quantoflife.ui.now

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skyfolk.quantoflife.databinding.CreateEventDialogBinding
import com.skyfolk.quantoflife.entity.*

class CreateEventDialogFragment(val quant: QuantBase) : BottomSheetDialogFragment() {
    private var dialogListener: DialogListener? = null

    private lateinit var binding: CreateEventDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateEventDialogBinding.inflate(inflater, container, false)


        binding.eventName.text = quant.name
        binding.eventDescription.text = quant.description

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
                    when (quant) {
                        is QuantBase.QuantRated -> binding.eventRating.rating.toInt()
                        is QuantBase.QuantMeasure -> binding.eventRatingNumeric.text.toString().toInt()
                        is QuantBase.QuantNote -> -1
                    },
                    binding.eventNote.text.toString()
                )
            )
            dismiss()
        }
        return binding.root
    }

    fun setDialogListener(listener: DialogListener) {
        dialogListener = listener
    }

    interface DialogListener {
        fun onConfirm(event: EventBase)
        fun onDecline()
    }
}