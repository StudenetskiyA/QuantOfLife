package com.skyfolk.quantoflife.ui.goals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skyfolk.quantoflife.databinding.CreateGoalDialogBinding
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.timeInterval.TimeInterval

class CreateGoalDialogFragment(
    private val goal: Goal?,
    private val settingsInteractor: SettingsInteractor
) : BottomSheetDialogFragment() {
    private var dialogListener: DialogListener? = null
    private lateinit var binding: CreateGoalDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateGoalDialogBinding.inflate(inflater, container, false)

        val categoryArray = mutableListOf(
            settingsInteractor.getCategoryName(QuantCategory.Physical),
            settingsInteractor.getCategoryName(QuantCategory.Emotion),
            settingsInteractor.getCategoryName(QuantCategory.Evolution),
            settingsInteractor.getCategoryName(QuantCategory.All)
        )
        val spinnerArrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryArray)
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerCategory.adapter = spinnerArrayAdapter

        if (goal != null) {
            binding.buttonDelete.visibility = View.VISIBLE
            val typePosition = if (goal.type.ordinal <= 3) goal.type.ordinal else categoryArray.size - 1
            binding.spinnerCategory.setSelection(typePosition)
            binding.spinnerPeriod.setSelection(
                when (goal.duration) {
                    is TimeInterval.Today -> 0
                    is TimeInterval.Week -> 1
                    is TimeInterval.Month -> 2
                    is TimeInterval.All -> 3
                    is TimeInterval.Selected -> 4
                }
            )
            binding.goalTarget.setText(goal.target.toString())
        }

        binding.buttonDelete.setOnClickListener {
            dialogListener?.onDelete(goal!!)
            dismiss()
        }

        binding.buttonOk.setOnClickListener {
            val category = when (binding.spinnerCategory.selectedItemPosition) {
                0 -> QuantCategory.Physical
                1 -> QuantCategory.Emotion
                2 -> QuantCategory.Evolution
                else -> QuantCategory.All
            }
            val interval = when (binding.spinnerPeriod.selectedItemPosition) {
                0 -> TimeInterval.Today
                1 -> TimeInterval.Week
                2 -> TimeInterval.Month
                3 -> TimeInterval.All
                else -> TimeInterval.All //TODO
            }

            if (binding.goalTarget.text.isEmpty()) {
                Toast.makeText(context, "Не заполненны числовые поля", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (goal != null) {
                dialogListener?.onConfirm(
                    Goal(
                        id = goal.id,
                        duration = interval,
                        target = binding.goalTarget.text.toString().toDouble(),
                        type = category
                    )
                )
            } else {
                dialogListener?.onConfirm(
                    Goal(
                        duration = interval,
                        target = binding.goalTarget.text.toString().toDouble(),
                        type = category
                    )
                )
            }
            dismiss()
        }
        return binding.root
    }

    fun setDialogListener(listener: DialogListener) {
        dialogListener = listener
    }

    interface DialogListener {
        fun onConfirm(goal: Goal)
        fun onDelete(goal: Goal)
        fun onDecline()
    }
}