package com.bookiibookii.bookiibookii.onboarding.steps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bookiibookii.bookiibookii.onboarding.steps.data.OnbState
import com.bookiibookii.bookiibookii.onboarding.steps.data.ReadingPace
import com.bookiibookii.bookiibookii.onboarding.steps.data.ReadingPreference
import com.bookiibookii.bookiibookii.onboarding.steps.data.RecordMethod

class OnbViewModel : ViewModel() {

    private val _state = MutableLiveData(OnbState())
    val state: LiveData<OnbState> = _state

    private val step1MaxSelect = 3

    // Step2: "아직 잘 모르겠어요" (단독 선택)
    private var step2Unknown = false

    private fun currentState(): OnbState = _state.value ?: OnbState()
    private fun updateState(newState: OnbState) {
        _state.value = newState
    }

    // Step1: 취향(ReadingPreference) 토글 (+ 최대 3개 제한)
    fun togglePreference(pref: ReadingPreference) {
        val cur = currentState()
        val next = cur.readingPreferences.toMutableSet()

        if (next.contains(pref)) {
            next.remove(pref)
            updateState(cur.copy(readingPreferences = next))
            return
        }

        if (next.size >= step1MaxSelect) return

        next.add(pref)
        updateState(cur.copy(readingPreferences = next))
    }

    // Step2: 상단 4개 토글 (모름 켜져 있으면 자동 해제)
    fun toggleRecordMethod(method: RecordMethod) {
        if (step2Unknown) step2Unknown = false

        val cur = currentState()
        val next = cur.recordMethods.toMutableSet()

        if (!next.add(method)) next.remove(method)

        updateState(cur.copy(recordMethods = next))
    }

    // Step2: 전체선택 토글 (모름 켜져 있으면 자동 해제)
    fun toggleAllRecordMethods() {
        if (step2Unknown) step2Unknown = false

        val cur = currentState()
        val all = RecordMethod.entries.toSet()
        val next = cur.recordMethods.toMutableSet()

        if (next.containsAll(all)) {
            next.removeAll(all)
        } else {
            next.addAll(all)
        }

        updateState(cur.copy(recordMethods = next))
    }

    // Step2: "아직 잘 모르겠어요" 토글 (단독)
    fun toggleStep2Unknown() {
        step2Unknown = !step2Unknown

        val cur = currentState()
        if (step2Unknown) {
            updateState(cur.copy(recordMethods = emptySet()))
        } else {
            updateState(cur)
        }
    }

    fun isStep2Unknown(): Boolean = step2Unknown

    fun isStep2AllSelected(): Boolean {
        val cur = currentState()
        val all = RecordMethod.entries.toSet()
        return all.isNotEmpty() && cur.recordMethods.containsAll(all)
    }

    // Step3: 단일 선택
    fun selectReadingPace(pace: ReadingPace) {
        val cur = currentState()
        updateState(cur.copy(readingPace = pace))
    }

    // 버튼 활성화 조건들 (Activity에서 사용)
    fun canGoStep2Next(): Boolean = currentState().readingPreferences.isNotEmpty()
    fun canGoStep3Next(): Boolean = currentState().recordMethods.isNotEmpty() || step2Unknown
    fun canFinish(): Boolean = currentState().readingPace != null
}