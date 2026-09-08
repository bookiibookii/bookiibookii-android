package com.bookiibookii.bookiibookii.common

import kotlinx.coroutines.CancellationException

/**
 * [runCatching]과 같지만 **코루틴 취소는 삼키지 않고 그대로 전파**한다.
 *
 * [runCatching]은 `Throwable`을 전부 잡으므로 `CancellationException`까지 `Result.failure`로
 * 감싼다. suspend 함수를 이렇게 감싸면, 상위에서 코루틴을 취소했는데도 실패로 취급되어
 * 사용자에게 "네트워크 오류"가 표시된다.
 *
 * 취소는 오류가 아니라 "더 이상 이 결과가 필요 없다"는 신호이므로 그대로 위로 던져야 한다.
 *
 * ```
 * runCatchingCancellable { RetrofitClient.grpApi().searchBooks(query) }
 *     .onSuccess { ... }
 *     .onFailure { ... }  // 취소는 여기로 오지 않는다
 * ```
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    runCatching(block).onFailure { if (it is CancellationException) throw it }
