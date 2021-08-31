package com.skyfolk.quantoflife.feeds

import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.entity.QuantCategory

fun getTotal(
    quants: List<QuantBase>,
    events: List<EventBase>,
    category: QuantCategory = QuantCategory.None
): Double {
    var total = 0.0

    for (event in events) {
        if (event is EventBase.EventRated) {
            val foundQuant = quants.filter { it.id == event.quantId }.firstOrNull()
            if (foundQuant is QuantBase.QuantRated) {
                for (bonus in foundQuant.bonuses) {
                    if (bonus.category == category || category == QuantCategory.None || category == QuantCategory.All) {
                        total += bonus.baseBonus + bonus.bonusForEachRating * event.rate
                    }
                }
            }
        }
    }
    return total
}

fun getTotalAverageStar(quants: List<QuantBase>, events: List<EventBase>): Double {
    if (events.isEmpty()) return 0.toDouble()

    return getStarTotal(quants, events).toDouble()/events.size.toDouble()
}

fun getTotalCount(events: List<EventBase>): Double {
    return events.size.toDouble()
}

fun getStarTotal(quants: List<QuantBase>, events: List<EventBase>): Int {
    var total = 0

    for (event in events) {
        if (event is EventBase.EventRated) {
            val foundQuant = quants.filter { it.id == event.quantId }
                .firstOrNull()
            if (foundQuant is QuantBase.QuantRated) {
                total += event.rate
            }
        }
    }
    return total
}
