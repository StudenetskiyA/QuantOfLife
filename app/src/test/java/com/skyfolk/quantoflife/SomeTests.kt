package com.skyfolk.quantoflife

import org.junit.Assert
import org.junit.Test

class SomeTests {
    enum class Status(val trackingId: String?) {
        PREPARING(null),
        DISPATCHED("27211"),
        DELIVERED("27211"),
    }

    @Test
    fun enum() {
       val a = Status.DELIVERED
       val b = Status.DELIVERED
        Assert.assertEquals(a, b)
        Assert.assertTrue(a == b)
    }
}