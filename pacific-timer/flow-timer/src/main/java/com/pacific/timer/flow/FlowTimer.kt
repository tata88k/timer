package com.pacific.timer.flow

import kotlin.concurrent.fixedRateTimer

class FlowTimer {

    fun start() {
        val timer = fixedRateTimer(period = 1000L) {

        }

        timer.purge()
    }
}