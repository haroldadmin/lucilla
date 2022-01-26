package com.haroldadmin.lucilla.pipeline.transforms

import com.haroldadmin.lucilla.pipeline.PipelineStep

/**
 * A [PipelineStep] that removes punctuation symbols from the
 * input text.
 */
public object StripPunctuationTransform : PipelineStep {
    private val symbols = listOf(',', '.', ':', '-', ';', '"', '\'', '!', '?', '(', ')')

    private val PunctuationRegex = Regex(
        symbols.joinToString(
            separator = "",
            prefix = "[",
            postfix = "]"
        ) { c -> """\$c""" }
    )

    override fun process(input: List<String>): List<String> {
        return input.map { process(it) }
    }

    public fun process(input: String): String {
        return input.replace(PunctuationRegex, "")
    }
}
