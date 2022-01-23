package com.haroldadmin.lucilla.core.text

import com.haroldadmin.lucilla.core.PipelineStep
import java.util.Locale

/**
 * A [PipelineStep] that lower-cases the input text
 * using the default [Locale]
 */
public object LowercaseTransform : PipelineStep {
    override fun process(input: List<String>): List<String> {
        return input.map { process(it) }
    }

    public fun process(input: String): String {
        return input.lowercase(Locale.getDefault())
    }
}