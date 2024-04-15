package com.alarm.clock.reminder.alarmclock.simplealarm.application.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

open class CoroutineLauncher : CoroutineScope {

    open val dispatcher: CoroutineDispatcher = Dispatchers.Main
    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = dispatcher + supervisorJob

    fun launch(action: suspend CoroutineScope.() -> Unit) = launch(block = action)

    fun cancelCoroutines() {
        supervisorJob.cancelChildren()
        supervisorJob.cancel()
    }
}