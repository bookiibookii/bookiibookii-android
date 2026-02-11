package com.bookiibookii.bookiibookii.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object ComRetryBus {

    private val _retryFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val retryFlow: SharedFlow<Unit> = _retryFlow

    fun emitRetry() {
        _retryFlow.tryEmit(Unit)
    }
}