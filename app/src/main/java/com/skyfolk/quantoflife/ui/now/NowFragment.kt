package com.skyfolk.quantoflife.ui.now

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.NowFragmentBinding
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.filterToArrayList
import com.skyfolk.quantoflife.setOnHideByTimeout
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.ui.feeds.TimeInterval
import com.skyfolk.quantoflife.ui.now.CreateEventDialogFragment.DialogListener
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class NowFragment : Fragment() {
    private val viewModel: NowViewModel by viewModel()
    private val settingsInteractor: SettingsInteractor by inject()

    private lateinit var binding: NowFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NowFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.toastState.observe(viewLifecycleOwner, { message ->
            if (message != "") {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        })
        viewModel.dialogState.observe(viewLifecycleOwner, { dialog ->
            if (dialog != null) {
                val fm: FragmentManager = requireActivity().supportFragmentManager
                dialog.show(fm, dialog.tag)
            }
        })
        viewModel.todayTotal.observe(viewLifecycleOwner, {
            binding.todayRating.text = String.format("%.1f", it)
            val todayScore = resources.getStringArray(R.array.today_score)
            binding.todayRatingScore.text = when (it) {
                in -Double.MAX_VALUE..2.0 -> todayScore[0]
                in 2.0..4.0 -> todayScore[1]
                in 4.0..6.0 -> todayScore[2]
                in 6.0..8.0 -> todayScore[3]
                in 8.0..Double.MAX_VALUE -> todayScore[4]
                else -> ""
            }
        })

        viewModel.listOfGoal.observe(viewLifecycleOwner, {
            if (it == null) binding.currentGoal.visibility = View.GONE
            it?.let {
                binding.currentGoal.updateViewState(it)
            }
        })

        binding.categoryPhysical.text = settingsInteractor.getCategoryName(QuantCategory.Physical)
        binding.categoryEmotion.text = settingsInteractor.getCategoryName(QuantCategory.Emotion)
        binding.categoryEvolution.text = settingsInteractor.getCategoryName(QuantCategory.Evolution)
        binding.categoryOther.text = settingsInteractor.getCategoryName(QuantCategory.Other)

        val listOfQuants: RecyclerView = binding.listOfPhysicalQuants
        val listOfEmotionQuants: RecyclerView = binding.listOfEmotionQuants
        val listOfEvolutionQuants: RecyclerView = binding.listOfEvolutionQuants
        val listOfOtherQuants: RecyclerView = binding.listOfOtherQuants
        listOfQuants.layoutManager =
            LinearLayoutManager(this.context, RecyclerView.HORIZONTAL, false)
        listOfEmotionQuants.layoutManager =
            LinearLayoutManager(this.context, RecyclerView.HORIZONTAL, false)
        listOfEvolutionQuants.layoutManager =
            LinearLayoutManager(this.context, RecyclerView.HORIZONTAL, false)
        listOfOtherQuants.layoutManager =
            LinearLayoutManager(this.context, RecyclerView.HORIZONTAL, false)

        val quantListClickListener: (quant: QuantBase) -> Unit = {
            val dialog = CreateEventDialogFragment(it)
            val theme = dialog.theme
            dialog.setDialogListener(object : DialogListener {
                override fun onConfirm(event: EventBase, name: String) {
                    val snackBar = Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Event '${name}' create",
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.setAction("Отмена") {
                    }
                    snackBar.setOnHideByTimeout {
                        viewModel.onEventCreated(event)
                    }
                    snackBar.show()
                }

                override fun onDecline() {
                }

                override fun onDelete(event: EventBase, name: String) {
                    //Событие не может быть удалено при его создании
                }
            })
            val fm: FragmentManager = requireActivity().supportFragmentManager
            dialog.show(fm, dialog.tag)
        }

        val quantListLongClickListener: (quant: QuantBase) -> Boolean = {
            viewModel.openCreateNewQuantDialog(it)
            true
        }

        lifecycleScope.launch {
            viewModel.listOfQuants.observe(viewLifecycleOwner) { quantsList ->
                val adapterPhysical =
                    QuantListDataAdapter(quantsList.filterToArrayList { it.primalCategory == QuantCategory.Physical },
                        { quant -> quantListClickListener(quant) },
                        { quant -> quantListLongClickListener(quant) })

                val adapterEmotion =
                    QuantListDataAdapter(quantsList.filterToArrayList { it.primalCategory == QuantCategory.Emotion },
                        { quant -> quantListClickListener(quant) },
                        { quant -> quantListLongClickListener(quant) })

                val adapterEvolution =
                    QuantListDataAdapter(quantsList.filterToArrayList { it.primalCategory == QuantCategory.Evolution },
                        { quant -> quantListClickListener(quant) },
                        { quant -> quantListLongClickListener(quant) })

                val adapterOther =
                    QuantListDataAdapter(quantsList.filterToArrayList { it.primalCategory == QuantCategory.Other },
                        { quant -> quantListClickListener(quant) },
                        { quant -> quantListLongClickListener(quant) })

                listOfQuants.adapter = adapterPhysical
                listOfEmotionQuants.adapter = adapterEmotion
                listOfEvolutionQuants.adapter = adapterEvolution
                listOfOtherQuants.adapter = adapterOther
            }
        }

        binding.fab.setOnClickListener {
            viewModel.openCreateNewQuantDialog(null)
        }
    }
}
