package com.skyfolk.quantoflife.ui

import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter

@BindingAdapter(
    value = [
        "selection",
        "itemSelectedListener"
    ]
)
internal fun Spinner.bindSetSelection(selection: Int, listener: AdapterView.OnItemSelectedListener) {

    setSelection(selection)

    onItemSelectedListener = listener
}

@BindingAdapter("dropDownItem")
internal fun Spinner.statisticFilerConfigure(stringArray: Array<String>) {

    adapter = ArrayAdapter(
        context,
        android.R.layout.simple_dropdown_item_1line,
        stringArray
    )
}