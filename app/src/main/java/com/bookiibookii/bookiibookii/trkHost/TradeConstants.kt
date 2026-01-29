package com.bookiibookii.bookiibookii.trkHost

enum class Role { HOST, GUEST }

enum class Phase {
    INIT,
    HOST_READING,
    HOST_SHIPPING_READY,
    HOST_SHIPPED,
    GUEST_READING,
    GUEST_SHIPPING_READY,
    GUEST_SHIPPED,
    FINISHED
}

object StepId {
    const val HOST_READING = "HOST_READING"
    const val HOST_SHIP = "HOST_SHIP"
    const val RECEIVE_CHECK = "RECEIVE_CHECK"
    const val GUEST_READING = "GUEST_READING"
    const val GUEST_SHIP = "GUEST_SHIP"
    const val RECEIVE_REGISTER = "RECEIVE_REGISTER"
    const val FINISH_HEADER = "FINISH_HEADER"
}


