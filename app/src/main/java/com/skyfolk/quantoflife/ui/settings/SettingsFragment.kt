package com.skyfolk.quantoflife.ui.settings

import android.app.DownloadManager
import android.app.TimePickerDialog
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.databinding.SettingsFragmentBinding
import com.skyfolk.quantoflife.ui.onboarding.OnBoardingActivity
import com.skyfolk.quantoflife.utils.showConfirmDialog
import com.skyfolk.quantoflife.utils.toDateWithoutHourAndMinutes
import com.skyfolk.quantoflife.utils.toShortDate
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File
import java.util.*


class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModel()

    private lateinit var binding: SettingsFragmentBinding

    private lateinit var permissionRequest: PermissionRequest

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentBinding.inflate(inflater, container, false)

        viewModel.toastState.observe(viewLifecycleOwner, { toast ->
            val text =
                when (toast) {
                    is SettingsFragmentToast.ImportComplete -> getString(
                        toast.textResourceId,
                        toast.eventsImported,
                        toast.eventsTypeImported
                    )
                    else -> getString(toast.textResourceId)
                }
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        })

        viewModel.dayStartTime.observe(viewLifecycleOwner, {
            val hour: String =
                if (it[Calendar.HOUR_OF_DAY] < 10) "0" + it[Calendar.HOUR_OF_DAY] else it[Calendar.HOUR_OF_DAY].toString()
            val minute: String =
                if (it[Calendar.MINUTE] < 10) "0" + it[Calendar.MINUTE] else it[Calendar.MINUTE].toString()
            binding.startHour.text =
                resources.getString(R.string.settings_set_day_start_time_current, hour, minute)
        })

        viewModel.downloadFile.observe(viewLifecycleOwner, { file ->
            val downloadManager =
                requireContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.addCompletedDownload(
                file.name,
                file.name,
                true,
                "text/plain",
                file.path,
                file.length(),
                true
            )
        })

        viewModel.permissionRequestState.observe(viewLifecycleOwner, { request ->
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    request.permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                QLog.d("Permission not granted")
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        request.permission
                    )
                ) {
                    QLog.d("Permission shouldShowRequestPermissionRationale")
                    permissionRequest = request
                    requestPermissions(
                        arrayOf(request.permission),
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                    )
                } else {
                    QLog.d("Permission request")
                    permissionRequest = request
                    requestPermissions(
                        arrayOf(request.permission),
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                    )
                }
            } else {
                QLog.d("Permission has already been granted")
                request.onGranted()
            }
        })

        binding.clearDbButton.setOnClickListener {
            requireContext().showConfirmDialog(
                title = resources.getString(R.string.clear_database_title),
                message = resources.getString(R.string.clear_database_message),
                positiveButtonTitle = resources.getString(R.string.delete),
                negativeButtonTitle = resources.getString(R.string.cancel)
            ) {
                viewModel.clearDatabase()
            }
        }

        binding.clearEventsButton.setOnClickListener {
            requireContext().showConfirmDialog(
                title = resources.getString(R.string.clear_events_title),
                message = resources.getString(R.string.clear_events_message),
                positiveButtonTitle = resources.getString(R.string.delete),
                negativeButtonTitle = resources.getString(R.string.cancel)
            ) {
                viewModel.clearEvents()
            }
        }

        binding.exportDbButton.setOnClickListener {

            val exportRealmPATH: File? = requireContext().getExternalFilesDir(null)
            val exportRealmFileName = "qol_backup.realm"
            val file = File(exportRealmPATH, exportRealmFileName)
            viewModel.saveDBToFile(file)

            if (file.exists()) {
                val intentShareFile = Intent(Intent.ACTION_SEND)
                intentShareFile.type = "application/pdf"
                val bmpUri = FileProvider.getUriForFile(requireContext(), "com.skyfolk.quantoflife.fileprovider", file)
                intentShareFile.putExtra(Intent.EXTRA_STREAM, bmpUri)
                intentShareFile.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_subject))
                intentShareFile.putExtra(Intent.EXTRA_TEXT, getString(R.string.export_text, System.currentTimeMillis().toDateWithoutHourAndMinutes()))

                startActivity(Intent.createChooser(intentShareFile, getString(R.string.export_title)))
            }
        }
        binding.importDbButton.setOnClickListener {
            viewModel.importAllEventsAndQuantsFromFile()
        }

        binding.submitCategoryNamesButton.setOnClickListener {
            startActivity(Intent(requireContext(), OnBoardingActivity::class.java))
        }

        binding.submitStartHour.setOnClickListener {
            TimePickerDialog(requireContext(), onTimeSelected, 0, 0, true)
                .show()
        }

        return binding.root
    }

    private val onTimeSelected = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        val result = ((hourOfDay * 60 * 60 * 1000) + (minute * 60 * 1000)).toLong()
        viewModel.setStartDayTime(result)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        QLog.d("onRequestPermissionsResult")
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionRequest.onGranted()
            } else {
                QLog.d("Permission decline")
            }
        }
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 989
    }

    sealed class SettingsFragmentToast(val textResourceId: Int) {
        object DatabaseCleared : SettingsFragmentToast(R.string.settings_database_cleared)
        object EventsCleared : SettingsFragmentToast(R.string.settings_events_cleared)
        object DatabaseExported : SettingsFragmentToast(R.string.settings_database_exported)
        data class ImportComplete(val eventsImported: Int, val eventsTypeImported: Int) :
            SettingsFragmentToast(R.string.settings_import_result)
    }
}