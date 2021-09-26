package com.skyfolk.quantoflife.ui.now

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.ui.goals.GoalsListDataAdapter
import com.skyfolk.quantoflife.ui.now.CreateEventDialogFragment.DialogListener
import com.skyfolk.quantoflife.utils.filterToArrayList
import com.skyfolk.quantoflife.utils.setOnHideByTimeout
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter
import uk.co.markormesher.android_fab.SpeedDialMenuCloseListener
import uk.co.markormesher.android_fab.SpeedDialMenuItem
import uk.co.markormesher.android_fab.SpeedDialMenuOpenListener

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

        lifecycleScope.launchWhenResumed {
            viewModel.todayTotal.collect {
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
            }
        }

        viewModel.listOfGoal.observe(viewLifecycleOwner, {
            if (it.size == 0) binding.goalsLayout.visibility = View.GONE
            else {
                binding.goalsLayout.visibility = View.VISIBLE
                binding.listOfGoals.layoutManager =
                    LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
                val adapterGoals = GoalsListDataAdapter(it, settingsInteractor) { goalPresent ->
                    viewModel.openCreateNewGoalDialog(
                        Goal(
                            goalPresent.id,
                            goalPresent.duration,
                            goalPresent.target,
                            goalPresent.type
                        )
                    )
                    true
                }
                binding.listOfGoals.adapter = adapterGoals
            }
        })

        binding.categoryPhysical.text = settingsInteractor.categoryNames[QuantCategory.Physical]
        binding.categoryEmotion.text = settingsInteractor.categoryNames[QuantCategory.Emotion]
        binding.categoryEvolution.text = settingsInteractor.categoryNames[QuantCategory.Evolution]
        binding.categoryOther.text = settingsInteractor.categoryNames[QuantCategory.Other]

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
            dialog.setDialogListener(object : DialogListener {
                override fun onConfirm(event: EventBase, name: String) {
                    val snackBar = Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        getString(R.string.now_event_created, name),
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.setAction(getString(R.string.cancel)) {
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

        binding.fab.setOnSpeedDialMenuOpenListener(object : SpeedDialMenuOpenListener {
            override fun onOpen(floatingActionButton: uk.co.markormesher.android_fab.FloatingActionButton) {
                binding.contentCover.visibility = View.VISIBLE
            }
        })
        binding.fab.setOnSpeedDialMenuCloseListener(object : SpeedDialMenuCloseListener {
            override fun onClose(floatingActionButton: uk.co.markormesher.android_fab.FloatingActionButton) {
                binding.contentCover.visibility = View.GONE
            }
        })

        binding.fab.speedDialMenuAdapter = speedDialMenuAdapter
        binding.fab.contentCoverEnabled = true
        binding.fab.setContentCoverColour(
            resources.getColor(
                R.color.transparent,
                requireContext().theme
            )
        )

    }

    private val speedDialMenuAdapter = object : SpeedDialMenuAdapter() {
        override fun getCount(): Int = 2

        override fun getMenuItem(context: Context, position: Int): SpeedDialMenuItem =
            when (position) {
                0 -> SpeedDialMenuItem(context, R.drawable.ic_pen, getString(R.string.menu_item_create_quant))
                1 -> SpeedDialMenuItem(context, R.drawable.ic_target, getString(R.string.menu_item_create_goal))
                else -> throw IllegalArgumentException("No menu item: $position")
            }

        override fun onMenuItemClick(position: Int): Boolean {
            when (position) {
                0 -> viewModel.openCreateNewQuantDialog(null)
                1 -> viewModel.openCreateNewGoalDialog(null)
            }
            return true
        }

        override fun onPrepareItemLabel(context: Context, position: Int, label: TextView) {
            label.setTypeface(label.typeface, Typeface.BOLD)
        }

        // rotate the "+" icon only
        override fun fabRotationDegrees(): Float = 135F//if (buttonIcon == 0) 135F else 0F
    }
}
