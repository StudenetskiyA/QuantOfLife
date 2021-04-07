package com.skyfolk.quantoflife.db

import android.util.Log
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import java.util.*


class RealmMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema

        Log.d("skyfolk-db","oldVersion = $oldVersion, newVersion = $newVersion")
        if (oldVersion == 5L) {
            schema["EventDbEntity"]
                .addField("id", String::class.java)
                .transform { oldObject ->
                    oldObject.setString("id", UUID.randomUUID().toString())
                }
                .addIndex("id")
                .addPrimaryKey("id")
        }

        if (oldVersion == 6L) {
            schema.create("GoalDbEntity")
                .addField("id", String::class.java, FieldAttribute.PRIMARY_KEY)
                .addField("duration", String::class.java)
                .addField("target", Double::class.java)
                .addField("type", String::class.java)
        }
    }
}