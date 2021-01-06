package com.skyfolk.quantoflife.db

import io.realm.DynamicRealm
import io.realm.RealmMigration
import java.util.*

class RealmMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema

        if (oldVersion == 5L) {
            schema["EventDbEntity"]
                .addField("id", String::class.java)
                .transform { oldObject ->
                    oldObject.setString("id", UUID.randomUUID().toString())
                }
                .addIndex("id")
                .addPrimaryKey("id")
        }
    }
}