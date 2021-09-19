package com.skyfolk.quantoflife.ui.create_quant

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.CreateQuantDialogBinding
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.settings.SettingsInteractor
import java.lang.reflect.Field

class CreateQuantDialogFragment(
    private val quant: QuantBase?,
    private val settingsInteractor: SettingsInteractor
) : BottomSheetDialogFragment() {
    private var dialogListener: DialogListener? = null

    private lateinit var binding: CreateQuantDialogBinding
    private var selectedIconIndex: Int? = null
    private lateinit var listOfIcons: ArrayList<String>
    private lateinit var adapter: IconsListDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateQuantDialogBinding.inflate(inflater, container, false)

        binding.listOfQuantsIcons.layoutManager = LinearLayoutManager(
            this.context,
            RecyclerView.HORIZONTAL,
            false
        )

        val categoryArray = mutableListOf(
            settingsInteractor.getCategoryName(QuantCategory.Physical),
            settingsInteractor.getCategoryName(QuantCategory.Emotion),
            settingsInteractor.getCategoryName(QuantCategory.Evolution),
            settingsInteractor.getCategoryName(QuantCategory.Other)
        )
        val spinnerArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.right_to_left_spinner, categoryArray)
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerCategory.adapter = spinnerArrayAdapter
        binding.bonusForPhysicalName.text = getString(R.string.bonuses, categoryArray[0])
        binding.bonusForEmotionName.text = getString(R.string.bonuses, categoryArray[1])
        binding.bonusForEvolutionName.text = getString(R.string.bonuses, categoryArray[2])

        if (quant != null) {
            binding.buttonDelete.visibility = View.VISIBLE

            binding.quantName.setText(quant.name)

            binding.spinnerCategory.setSelection(quant.primalCategory.ordinal)
            binding.spinnerQuantType.setSelection(
                when (quant) {
                    is QuantBase.QuantRated -> 0
                    is QuantBase.QuantNote -> 1
                    else -> 0
                }
            )

            if (quant is QuantBase.QuantRated) {
                quant.getBonusFor(QuantCategory.Physical)?.let {
                    binding.bonusForPhysicalBase.setText(it.baseBonus.toString())
                    binding.bonusForPhysicalForEach.setText(it.bonusForEachRating.toString())
                }
                quant.getBonusFor(QuantCategory.Emotion)?.let {
                    binding.bonusForEmotionBase.setText(it.baseBonus.toString())
                    binding.bonusForEmotionForEach.setText(it.bonusForEachRating.toString())
                }
                quant.getBonusFor(QuantCategory.Evolution)?.let {
                    binding.bonusForEvolutionBase.setText(it.baseBonus.toString())
                    binding.bonusForEvolutionForEach.setText(it.bonusForEachRating.toString())
                }

            }

            binding.noteForQuant.setText(quant.description)
        }

        listOfIcons = getListOfQuantDrawableResources(requireContext())
        adapter = IconsListDataAdapter(listOfIcons, quant?.icon) {
            selectedIconIndex =
                if (listOfIcons.indexOf(it) == selectedIconIndex) null else listOfIcons.indexOf(
                    it
                )
            adapter.update(selectedIconIndex)
        }
        binding.listOfQuantsIcons.adapter = adapter
        if (quant != null) {
            listOfIcons.indexOf(quant.icon).let {
                binding.listOfQuantsIcons.scrollToPosition(it)
                selectedIconIndex = it
            }
        }

        binding.spinnerQuantType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val viewState = if (position == 0) View.VISIBLE else View.GONE
                    binding.bonusForPhysical.visibility = viewState
                    binding.bonusForEmotion.visibility = viewState
                    binding.bonusForEvolution.visibility = viewState
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.buttonDelete.setOnClickListener {
            dialogListener?.onDelete(quant!!)
            dismiss()
        }

        binding.aboutTypeButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialog)
            builder.setTitle("Тип события")
                .setMessage(resources.getString(R.string.about_type_quant))
                .setPositiveButton("ОК") { dialog, _ ->
                    dialog.cancel()
                }
            builder.show()
        }

        binding.buttonOk.setOnClickListener {
            var createdQuant : QuantBase? = null
            val name = binding.quantName.text.toString()
            val quantType = when (binding.spinnerQuantType.selectedItemPosition) {
                0 -> QuantBase.QuantRated::class.java.name
                else -> QuantBase.QuantNote::class.java.name
            }
            val category = when (binding.spinnerCategory.selectedItemPosition) {
                0 -> QuantCategory.Physical
                1 -> QuantCategory.Emotion
                2 -> QuantCategory.Evolution
                else -> QuantCategory.Other
            }

            if (name.length < 2) {
                Toast.makeText(context, "Слишком короткое название", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedIconIndex == null || selectedIconIndex!! >= listOfIcons.size) {
                Toast.makeText(context, "Не выбрана иконка", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            when (quantType) {
                QuantBase.QuantRated::class.java.name -> {
                    val listOfQuantBonuses = ArrayList<QuantBonusBase.QuantBonusRated>()
                    if (binding.bonusForPhysicalBase.text.isEmpty() && binding.bonusForPhysicalForEach.text.isEmpty() &&
                        binding.bonusForEmotionBase.text.isEmpty() && binding.bonusForEmotionForEach.text.isEmpty() &&
                        binding.bonusForEvolutionBase.text.isEmpty() && binding.bonusForEvolutionForEach.text.isEmpty()
                    ) {
                        Toast.makeText(context, "Не заполненны числовые поля", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnClickListener
                    }

                    if (binding.bonusForPhysicalBase.text.isNotEmpty() || binding.bonusForPhysicalForEach.text.isNotEmpty()) {
                        listOfQuantBonuses.add(
                            QuantBonusBase.QuantBonusRated(
                                QuantCategory.Physical,
                                if (binding.bonusForPhysicalBase.text.isNotEmpty()) binding.bonusForPhysicalBase.text.toString()
                                    .toDouble() else 0.0,
                                if (binding.bonusForPhysicalForEach.text.isNotEmpty()) binding.bonusForPhysicalForEach.text.toString()
                                    .toDouble() else 0.0
                            )
                        )
                    }
                    if (binding.bonusForEmotionBase.text.isNotEmpty() || binding.bonusForEmotionForEach.text.isNotEmpty()) {
                        listOfQuantBonuses.add(
                            QuantBonusBase.QuantBonusRated(
                                QuantCategory.Emotion,
                                if (binding.bonusForEmotionBase.text.isNotEmpty()) binding.bonusForEmotionBase.text.toString()
                                    .toDouble() else 0.0,
                                if (binding.bonusForEmotionForEach.text.isNotEmpty()) binding.bonusForEmotionForEach.text.toString()
                                    .toDouble() else 0.0
                            )
                        )
                    }
                    if (binding.bonusForEvolutionBase.text.isNotEmpty() || binding.bonusForEvolutionForEach.text.isNotEmpty()) {
                        listOfQuantBonuses.add(
                            QuantBonusBase.QuantBonusRated(
                                QuantCategory.Evolution,
                                if (binding.bonusForEvolutionBase.text.isNotEmpty()) binding.bonusForEvolutionBase.text.toString()
                                    .toDouble() else 0.0,
                                if (binding.bonusForEvolutionForEach.text.isNotEmpty()) binding.bonusForEvolutionForEach.text.toString()
                                    .toDouble() else 0.0
                            )
                        )
                    }
                    createdQuant = QuantBase.QuantRated(
                        name = name,
                        icon = listOfIcons[selectedIconIndex!!],
                        primalCategory = category,
                        bonuses = listOfQuantBonuses,
                        description = binding.noteForQuant.text.toString()
                    )
                }
                // Deprecated
                QuantBase.QuantMeasure::class.java.name -> {
                    createdQuant = QuantBase.QuantMeasure(
                        name = name,
                        icon = listOfIcons[selectedIconIndex!!],
                        primalCategory = category,
                        description = binding.noteForQuant.text.toString()
                    )
                }
                QuantBase.QuantNote::class.java.name -> {
                    createdQuant = QuantBase.QuantNote(
                        name = name,
                        icon = listOfIcons[selectedIconIndex!!],
                        primalCategory = category,
                        description = binding.noteForQuant.text.toString()
                    )
                }
            }

            if (createdQuant != null) {
                if (quant != null) {
                    createdQuant.id = quant.id
                }
                dialogListener?.onConfirm(createdQuant)

                dismiss()
            }
        }
        return binding.root
    }

    fun setDialogListener(listener: DialogListener) {
        dialogListener = listener
    }

    interface DialogListener {
        fun onConfirm(quant: QuantBase)
        fun onDelete(quant: QuantBase)
        fun onDecline()
    }
}


fun getListOfQuantDrawableResources(context: Context): ArrayList<String> {
    val packageName = context.packageName
    val resourcesClass = Class.forName("$packageName.R")
    val subclasses = resourcesClass.declaredClasses
    var drawableClass: Class<*>? = null
    for (subclass in subclasses) {
        if ("com.skyfolk.quantoflife.R.drawable" == subclass.canonicalName) {
            drawableClass = subclass
            break
        }
    }
    val result: ArrayList<String> = ArrayList()

    if (drawableClass == null) return result

    val drawables: Array<Field> = drawableClass.fields
    for (dr in drawables) {
        if (dr.name.startsWith("quant_")) {
            result.add(dr.name)
        }
    }

    return result
}