package com.skyfolk.quantoflife.db

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration

class DBInteractor(context: Context) {
    var realm: Realm? = null

    init {
        initRealm(context)
    }

    fun getDBPath() : String {
        return getDB().path
    }

    fun getDB() : Realm {
        if (realm == null) realm = Realm.getDefaultInstance()
        return realm!!
    }

    fun close() {
        realm?.close()
        realm = null
    }

    private fun initRealm(context: Context) {
        Realm.init(context)
        val config = RealmConfiguration.Builder()
            .name("qol.realm")
            .schemaVersion(8)
            .migration(RealmMigration())
            .build()
        Realm.setDefaultConfiguration(config)
    }
}