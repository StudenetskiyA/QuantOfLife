package com.skyfolk.quantoflife.ui.settings

import android.content.Context
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.db.DBInteractor
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.import.ImportInteractor
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.utils.SingleLiveEvent
import com.skyfolk.quantoflife.utils.toCalendarOnlyHourAndMinute
import kotlinx.coroutines.launch
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class SettingsViewModel(
    private val context: Context,
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val dbInteractor: DBInteractor,
    private val importInteractor: ImportInteractor
) : ViewModel() {
    private val _toastState = SingleLiveEvent<String>()
    val toastState: LiveData<String> get() = _toastState

    private val _dayStartTime = SingleLiveEvent<Calendar>().apply {
        value = settingsInteractor.getStartDayTime().toCalendarOnlyHourAndMinute()
    }
    val dayStartTime: LiveData<Calendar> get() = _dayStartTime

    private val _downloadFile = SingleLiveEvent<File>()
    val downloadFile: LiveData<File> get() = _downloadFile

    private val _permissionRequestState = SingleLiveEvent<PermissionRequest>()
    val permissionRequestState: LiveData<PermissionRequest> get() = _permissionRequestState

    private val path =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private val file = File(path, "qol_backup.realm")
    private val localFile = File(path, "qol_base.realm")

    fun clearDatabase() {
        eventsStorageInteractor.clearDataBase()
        _toastState.value = "Database cleared!"
    }

    fun clearEvents() {
        eventsStorageInteractor.clearEvents()
        _toastState.value = "Events cleared!"
    }

    fun importAllEventsAndQuantsFromFile() {
        _permissionRequestState.value =
            PermissionRequest("android.permission.WRITE_EXTERNAL_STORAGE") {
                viewModelScope.launch {
                    // TODO If file not exist
                    val inputStream = FileInputStream(File(file.path))
                    importInteractor.importAllFromFile(inputStream) { quantsImported, eventsImported ->
                        _toastState.value =
                            "Импорт успешен\nИмпортировано новых типов событий - $quantsImported\nИмпортировано новых событий - $eventsImported"
                    }
                }
            }
    }

    fun saveDBToFile() {
        _permissionRequestState.value =
            PermissionRequest("android.permission.WRITE_EXTERNAL_STORAGE") {
                file.delete()

                dbInteractor.getDB().writeCopyTo(file)
                _downloadFile.value = file
                _toastState.value = "Архив сохранен в папку \"Загрузки\" "
            }
    }

    fun setStartDayTime(timeInMillis: Long) {
        settingsInteractor.setStartDayTime(timeInMillis)
        _dayStartTime.value = timeInMillis.toCalendarOnlyHourAndMinute()
    }

    private fun copyBundledRealmFile(inputFileName: String, outFileName: String): String? {
        try {
            val outputStream = FileOutputStream(File(outFileName))
            //val inputStream = FileInputStream(File(inputFileName))
            val inputStream = context.getResources().openRawResource(R.raw.qol_base)
            val buf = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buf).also { bytesRead = it } > 0) {
                outputStream.write(buf, 0, bytesRead)
            }
            outputStream.close()
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}

//val inputStream = FileInputStream(File(inputFileName))
//val inputStream = context.getResources().openRawResource(R.raw.qol_base)

data class PermissionRequest(val permission: String, val onGranted: () -> Unit)