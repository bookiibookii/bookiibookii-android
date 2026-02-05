package com.bookiibookii.bookiibookii.onboarding.steps.data

data class OnbState(
    val readingPreferences: Set<ReadingPreference> = emptySet(), // Step1
    val recordMethods: Set<RecordMethod> = emptySet(),            // Step2
    val readingPace: ReadingPace? = null                          // Step3
)