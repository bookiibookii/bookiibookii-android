package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressItemDTO
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.TrackerDeliveryAddressDisplay

fun DeliveryAddressItemDTO?.toDisplay(): TrackerDeliveryAddressDisplay = TrackerDeliveryAddressDisplay(
    receiverName = this?.receiverName.orEmpty(),
    phoneNumber = this?.phoneNumber.orEmpty(),
    address = this?.address.orEmpty(),
    addressDetail = this?.addressDetail.orEmpty(),
)
