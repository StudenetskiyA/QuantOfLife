package com.skyfolk.quantoflife

import android.util.Log

class QLog {
    companion object {
        fun d(tag: String, message: String = "log-level-skyfolk",) {
            Log.d(tag, message)
        }

        fun t(tag: String, message: String = "log-level-skyfolk",) {
            println("$tag: $message")
        }
    }
}