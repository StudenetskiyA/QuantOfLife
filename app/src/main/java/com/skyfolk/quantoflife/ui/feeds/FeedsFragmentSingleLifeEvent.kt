package com.skyfolk.quantoflife.ui.feeds

import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantBase

sealed class FeedsFragmentSingleLifeEvent {
    data class ShowEditEventDialog(val quant: QuantBase, val event: EventBase?) : FeedsFragmentSingleLifeEvent()
}
