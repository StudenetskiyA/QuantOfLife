package com.skyfolk.quantoflife

import com.google.android.material.snackbar.Snackbar
import com.skyfolk.quantoflife.entity.QuantBase

fun Snackbar.setOnHideByTimeout(onTimeout : () -> Unit) {
    this.addCallback(object : Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            if (event == 2) {
                onTimeout()
            }
        }
    })
}

fun ArrayList<QuantBase>.filterToArrayList(predicate: (QuantBase) -> Boolean): ArrayList<QuantBase> {
    return ArrayList(this.filter(predicate))
}