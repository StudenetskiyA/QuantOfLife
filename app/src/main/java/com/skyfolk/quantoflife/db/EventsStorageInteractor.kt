package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.utils.getStartDateCalendar
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class EventsStorageInteractor(private val dbInteractor: DBInteractor) {
    fun clearDataBase() {
        dbInteractor.getDB().executeTransactionAsync {
            it.deleteAll()
        }
    }

    fun clearEvents() {
        dbInteractor.getDB().executeTransactionAsync {
            it.delete(EventDbEntity::class.java)
            for (quant in it.where(QuantDbEntity::class.java).findAll()) {
                quant.usageCount = 0
            }
        }
    }

    fun addEventToDB(event: EventBase, onComplete: () -> Unit) {
        var rate: Double? = null
        var numericValue: Double? = null

        when (event) {
            is EventBase.EventRated -> {
                rate = event.rate
            }
            is EventBase.EventMeasure -> {
                numericValue = event.value
            }
            is EventBase.EventNote -> {
            }
        }
        val eventDbElement =
            EventDbEntity(event.quantId, event.date, rate, numericValue, event.note)

        dbInteractor.getDB().executeTransactionAsync({
            val existEvent = existEventOrNull(it, event)
            if (existEvent != null) {
                existEvent.date = event.date
                existEvent.rate = rate
                existEvent.numericValue = numericValue
                existEvent.note = event.note
            } else {
                it.insertOrUpdate(eventDbElement)
            }
        }, {
            onComplete()
        }, null)
    }

    fun deleteEvent(event: EventBase, onComplete: () -> Unit) {
        dbInteractor.getDB().executeTransactionAsync({
            existEventOrNull(it, event)?.deleteFromRealm()
        }, {
            onComplete()
        }, null)
    }

    fun getAllEventsYears(startDayTime: Long): List<String> {
        //  TODO Это очень, очень неоптимальный поиск

        val minute = 60 * 1000
        val hour = 60 * minute

        return dbInteractor
            .getDB()
            .where(EventDbEntity::class.java)
            .findAll()
            .map { it.date }
            .sortedBy { it }
            .map {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it

                val dayTime: Long =
                    (calendar[Calendar.HOUR_OF_DAY] * hour + calendar[Calendar.MINUTE] * minute +
                            calendar[Calendar.SECOND] * 1000).toLong()
                if (dayTime < startDayTime) {
                    calendar[Calendar.DAY_OF_YEAR]--
                }

                calendar.getStartDateCalendar(TimeInterval.Year, 0)[Calendar.YEAR].toString()
            }
            .distinct()
    }

    suspend fun getAllEvents(): ArrayList<EventBase> = withContext(Dispatchers.IO) {
        val result = ArrayList<EventBase>()

        dbInteractor.getDB().freeze().where(EventDbEntity::class.java).findAll()
            .sortedBy { it.date }
            .forEach { eventDbEntity ->
                when {
                    (eventDbEntity.rate != null) -> {
                        result.add(
                            EventBase.EventRated(
                                eventDbEntity.id,
                                eventDbEntity.quantId,
                                eventDbEntity.date,
                                eventDbEntity.note,
                                eventDbEntity.rate!!
                            )
                        )
                    }
                    (eventDbEntity.numericValue != null) -> {
                        result.add(
                            EventBase.EventMeasure(
                                eventDbEntity.id,
                                eventDbEntity.quantId,
                                eventDbEntity.date,
                                eventDbEntity.note,
                                eventDbEntity.numericValue!!
                            )
                        )
                    }
                    else -> {
                        result.add(
                            EventBase.EventNote(
                                eventDbEntity.id,
                                eventDbEntity.quantId,
                                eventDbEntity.date,
                                eventDbEntity.note
                            )
                        )
                    }
                }
            }

        return@withContext result
    }

    suspend fun alreadyHaveEvent(event: EventBase): Boolean = withContext(Dispatchers.IO) {
        return@withContext dbInteractor
            .getDB()
            .freeze()
            .where(EventDbEntity::class.java)
            .equalTo("id", event.id)
            .findFirst() != null
    }

    private fun existEventOrNull(realm: Realm, event: EventBase): EventDbEntity? {
        return realm.where(EventDbEntity::class.java)
            .equalTo("id", event.id)
            .findFirst()
    }
}