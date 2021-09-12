package com.skyfolk.quantoflife.import

import android.content.Context
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.db.DBInteractor
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.settings.SettingsInteractor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ImportInteractor(
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val dbInteractor: DBInteractor,
    private val inputDefaultQuantsStream: InputStream
) {

    suspend fun importAllFromFile(inputStream: InputStream, onComplete: (Int, Int) -> Unit) {
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
        copyBundledRealmFile(inputStream, mainPath)

        quantsImported =
            quantsStorageInteractor.getAllQuantsList(true).size - quantsImported
        eventsImported = eventsStorageInteractor.getAllEvents().size - eventsImported

        //Merge
        for (event in oldEvents) {
            if (!eventsStorageInteractor.alreadyHaveEvent(event)) {
                eventsStorageInteractor.addEventToDB(event) {}
            }
        }
        for (quant in oldQuants) {
            if (!quantsStorageInteractor.alreadyHaveQuant(quant)) {
                quantsStorageInteractor.addQuantToDB(quant) {}
            }
        }

        onComplete(quantsImported, eventsImported)
    }

    suspend fun importEventsFromRaw(onComplete: () -> Unit) {
        importAllFromFile(inputStream = inputDefaultQuantsStream) { quantsImported, eventsImported ->
            QLog.d("skyfolk-import",
                "Импорт успешен\nИмпортировано новых типов событий - $quantsImported\nИмпортировано новых событий - $eventsImported")
            onComplete()
        }
    }

    private fun copyBundledRealmFile(inputStream: InputStream, outFileName: String) {
        try {
            val outputStream = FileOutputStream(File(outFileName))
            val buf = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buf).also { bytesRead = it } > 0) {
                outputStream.write(buf, 0, bytesRead)
            }
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}