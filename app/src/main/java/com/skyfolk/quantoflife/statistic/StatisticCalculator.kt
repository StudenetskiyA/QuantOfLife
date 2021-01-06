package com.skyfolk.quantoflife.statistic

import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantCategory

fun getTotal(events: List<EventBase>, category: QuantCategory = QuantCategory.None) : Double {
    var total = 0.0

    for (event in events) {
        if (event is EventBase.EventRated) {
            for (bonus in event.bonuses) {
                if (bonus.category == category || category == QuantCategory.None) {
                    total += bonus.value
                }
            }
        }
    }
    return total
}

