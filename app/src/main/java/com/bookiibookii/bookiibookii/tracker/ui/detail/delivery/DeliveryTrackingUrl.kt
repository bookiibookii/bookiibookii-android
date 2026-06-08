package com.bookiibookii.bookiibookii.tracker.ui.detail.delivery

// 택배사 코드 → 배송 조회 외부 URL
// {trackingNumber} 자리에 운송장 번호를 넣어 각 택배사 조회 페이지로 연결
// 알 수 없는 코드/빈 송장번호면 null (호출부에서 안내 처리)
fun deliveryTrackingUrl(companyCode: String?, trackingNumber: String?): String? {
    val number = trackingNumber?.takeIf { it.isNotBlank() } ?: return null
    val template = when (companyCode) {
        "CJ_LOGISTICS" -> "https://www.cjlogistics.com/ko/tool/parcel/newTracking?gnbInvcNo=%s"
        "HANJIN" -> "https://www.hanjin.com/kor/CMS/DeliveryMgr/WaybillResult.do?mCode=MN038&schLang=KR&wblnumText2=%s"
        "LOTTE" -> "https://www.lotteglogis.com/home/reservation/tracking/linkView?InvNo=%s"
        "POST_OFFICE" -> "https://service.epost.go.kr/trace.RetrieveDomRigiTraceList.comm?displayHeader=N&sid1=%s"
        "LOGEN" -> "https://www.ilogen.com/m/personal/trace.pop/%s"
        "CU" -> "https://www.cupost.co.kr/postbox/delivery/localResult.cupost?invoice_no=%s"
        "GS" -> "https://www.cvsnet.co.kr/invoice/tracking.do?invoice_no=%s"
        else -> return null
    }
    return template.format(number)
}
