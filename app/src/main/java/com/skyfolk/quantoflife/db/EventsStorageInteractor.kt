package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.*
import io.realm.RealmList

class EventsStorageInteractor(private val dbInteractor: DBInteractor) {
    fun clearDataBase() {
        dbInteractor.getDB().executeTransaction { dbInteractor.getDB().deleteAll() } //TODO Separate
    }

    fun addEventToDB(event: EventBase) {
        val bonuses = RealmList<EventBonusDbEntity>()
        var rate: Int? = null
        var value: Int? = null

        when (event) {
            is EventBase.EventRated -> {
                for (bonus in event.bonuses) {
                    bonuses.add(EventBonusDbEntity.parseEventBonusBase(bonus))
                }
                rate = event.rate
            }
//            is EventBase.EventRatedFact -> {
//                for (bonus in event.bonuses) {
//                    bonuses.add(EventBonus.parseEventBonusBase(bonus))
//                }
//            }
            is EventBase.EventMeasure -> {
                value = event.value
            }
            is EventBase.EventNote -> {
            }
        }
        val eventDbElement = EventDbEntity(event.name, event.date, rate, value, bonuses, event.note)

        dbInteractor.getDB().executeTransaction {
            dbInteractor.getDB().insertOrUpdate(eventDbElement)
        }
    }

    fun getAllEvents(): ArrayList<EventBase> {
        val result = ArrayList<EventBase>()
        for (r in dbInteractor.getDB().where(EventDbEntity::class.java).findAll().sortedBy { it.date }) {
            when {
                (r.bonusDbEntities.isNotEmpty() && r.rate != null) -> {
                    val bonuses = ArrayList<EventBonusBase>()
                    for (bonus in r.bonusDbEntities) {
                        bonuses.add(
                            EventBonusBase(
                                QuantCategory.valueOf(bonus.categoryDescription),
                                bonus.value
                            )
                        )
                    }
                    result.add(EventBase.EventRated(r.name, r.date, r.note, r.rate!!, bonuses))
                }
                (r.bonusDbEntities.isEmpty() && r.rate != null) -> {
                    result.add(EventBase.EventMeasure(r.name, r.date, r.note, r.rate!!))
                }
                else -> {
                    result.add(EventBase.EventNote(r.name, r.date, r.note))
                }
            }
        }
        return result
    }

    //TODO This is bad implementation
    fun alreadyHaveEvent(event: EventBase): Boolean {
        for (storedEvent in getAllEvents()) {
            if (event.isEqual(storedEvent)) return true
        }
        return false
    }

//    private fun getLastDate(): String {
//        val list = getAllSongsFileExist().sortedByDescending { it.added.toLocalDateTime() }
//        return if (list.isEmpty()) "" else list[0].added
//    }
//
//    fun getAllSongsFileExist(): ArrayList<Song> {
//        val result = ArrayList<Song>()
//
//        val resultRealm = realm.where(Song::class.java).equalTo("isFileExist", true).findAll()
//
//        for (r in resultRealm) {
//            result.add(r)
//        }
//        return result
//    }


//
//    fun getSongByID(songID: String): Song? {
//        return if (realm.where(Song::class.java).equalTo("ID", songID).count() > 0)
//            realm.where(Song::class.java).equalTo("ID", songID).findFirst()
//        else null
//    }

}