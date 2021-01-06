package com.skyfolk.quantoflife

import android.util.Log

class QLog {
    companion object {
        fun d(message: String, tag: String = "log-level-skyfolk",) {
            Log.d(tag, message)
        }
    }
}