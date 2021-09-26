package com.skyfolk.quantoflife.ui.settings

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.db.DBInteractor
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.import.ImportInteractor
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.utils.SingleLiveEvent
import com.skyfolk.quantoflife.utils.toCalendarOnlyHourAndMinute
import kotlinx.coroutines.launch
import java.io.*
import java.util.*
import com.skyfolk.quantoflife.*


class SettingsViewModel(
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val dbInteractor: DBInteractor,
    private val importInteractor: ImportInteractor
) : ViewModel() {
    private val _toastState = SingleLiveEvent<SettingsFragment.SettingsFragmentToast>()
    val toastState: LiveData<SettingsFragment.SettingsFragmentToast> get() = _toastState

    private val _dayStartTime = SingleLiveEvent<Calendar>().apply {
        value = settingsInteractor.startDayTime.toCalendarOnlyHourAndMinute()
    }
    val dayStartTime: LiveData<Calendar> get() = _dayStartTime

    private val _downloadFile = SingleLiveEvent<File>()
    val downloadFile: LiveData<File> get() = _downloadFile

    private val _permissionRequestState = SingleLiveEvent<PermissionRequest>()
    val permissionRequestState: LiveData<PermissionRequest> get() = _permissionRequestState

    private val path =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private val file = File(path, "qol_backup.realm")

    fun clearDatabase() {
        eventsStorageInteractor.clearDataBase()
        _toastState.value = SettingsFragment.SettingsFragmentToast.DatabaseCleared
    }

    fun clearEvents() {
        eventsStorageInteractor.clearEvents()
        _toastState.value = SettingsFragment.SettingsFragmentToast.EventsCleared
    }

    fun importAllEventsAndQuantsFromFile() {
        _permissionRequestState.value =
            PermissionRequest("android.permission.WRITE_EXTERNAL_STORAGE") {
                viewModelScope.launch {
                    // TODO If file not exist
                    val inputStream = FileInputStream(File(file.path))
                    importInteractor.importAllFromFile(inputStream) { quantsImported, eventsImported ->
                        _toastState.value = SettingsFragment.SettingsFragmentToast.ImportComplete(
                            eventsImported,
                            quantsImported
                        )
                    }
                }
            }
    }

    fun saveDBToFile(file: File) {
        viewModelScope.launch {
            file.delete()
            dbInteractor.getDB().writeCopyTo(file)
        }
    }

    fun setStartDayTime(timeInMillis: Long) {
        settingsInteractor.startDayTime = timeInMillis
        _dayStartTime.value = timeInMillis.toCalendarOnlyHourAndMinute()
    }
}

data class PermissionRequest(val permission: String, val onGranted: () -> Unit)