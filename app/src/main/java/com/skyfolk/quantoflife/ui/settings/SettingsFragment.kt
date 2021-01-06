package com.skyfolk.quantoflife.ui.settings

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.databinding.SettingsFragmentBinding
import com.skyfolk.quantoflife.db.DBInteractor
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.ui.onboarding.OnBoardingActivity
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel


class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModel()

    private val dbInteractor: DBInteractor by inject()

    private lateinit var binding: SettingsFragmentBinding

    private lateinit var permissionRequest: PermissionRequest

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentBinding.inflate(inflater, container, false)

        viewModel.toastState.observe(viewLifecycleOwner, { message ->
            if (message != "") {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        })

        viewModel.downloadFile.observe(viewLifecycleOwner, { file ->
            val downloadManager = requireContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager
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
            viewModel.clearDatabase()
        }
        binding.exportDbButton.setOnClickListener {
            viewModel.saveDBToFile()
        }
        binding.importDbButton.setOnClickListener {
            viewModel.importAllEventsAndQuantsFromFile()
        }

        binding.submitCategoryNamesButton.setOnClickListener {
            startActivity(Intent(requireContext(), OnBoardingActivity::class.java))
        }

        return binding.root
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
}