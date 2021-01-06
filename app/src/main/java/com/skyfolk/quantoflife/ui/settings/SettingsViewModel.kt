package com.skyfolk.quantoflife.ui.settings

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.skyfolk.quantoflife.db.DBInteractor
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.utils.SingleLiveEvent
import java.io.*

class SettingsViewModel(
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val dbInteractor: DBInteractor
) : ViewModel() {
    private val _toastState = SingleLiveEvent<String>()
    val toastState: LiveData<String> get() = _toastState

    private val _downloadFile = SingleLiveEvent<File>()
    val downloadFile: LiveData<File> get() = _downloadFile

    private val _permissionRequestState = SingleLiveEvent<PermissionRequest>()
    val permissionRequestState: LiveData<PermissionRequest> get() = _permissionRequestState

    private val path =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private val file = File(path, "qol_backup.realm")

    fun clearDatabase() {
        eventsStorageInteractor.clearDataBase()
        _toastState.value = "Database cleared!"
    }

    fun importAllEventsAndQuantsFromFile() {
        val mainPath = dbInteractor.getDBPath()
        //Copy
        var eventsImported = 0
        var quantsImported = 0
        val oldEvents = ArrayList<EventBase>()
        for (oldEvent in eventsStorageInteractor.getAllEvents()) {
            eventsImported++
            oldEvents.add(oldEvent.copy())
        }
        val oldQuants = ArrayList<QuantBase>()
        for (oldQuant in quantsStorageInteractor.getAllQuantsList(true)) {
            quantsImported++
            oldQuants.add(oldQuant.copy())
        }

        dbInteractor.close()
        val restoreFilePath = file.path
        copyBundledRealmFile(restoreFilePath, mainPath)

        //Merge
        for (event in oldEvents) {
            if (!eventsStorageInteractor.alreadyHaveEvent(event)) {
            eventsStorageInteractor.addEventToDB(event)
            }
        }
        for (quant in oldQuants) {
            if (!quantsStorageInteractor.alreadyHaveQuant(quant)) {
                quantsStorageInteractor.addQuantToDB(quant)
            }
        }

        //Count
        _toastState.value = "Импорт успешен\nИмпортировано новых типов событий - $quantsImported\nИмпортировано новых событий - $eventsImported"
    }

    fun saveDBToFile() {
        _permissionRequestState.value =
            PermissionRequest("android.permission.READ_EXTERNAL_STORAGE") {
                file.delete()

                dbInteractor.getDB().writeCopyTo(file)
                _downloadFile.value = file
                _toastState.value = "Архив сохранен в папку \"Загрузки\" "
            }
    }

    private fun copyBundledRealmFile(oldFilePath: String, outFileName: String): String? {
        try {
            val file = File(outFileName)
            val outputStream = FileOutputStream(file)
            val inputStream = FileInputStream(File(oldFilePath))
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

data class PermissionRequest(val permission: String, val onGranted: () -> Unit)