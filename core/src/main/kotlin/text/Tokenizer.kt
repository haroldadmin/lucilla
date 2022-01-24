package com.haroldadmin.lucilla.core.text

import com.haroldadmin.lucilla.core.PipelineStep
import opennlp.tools.tokenize.WhitespaceTokenizer
import opennlp.tools.tokenize.Tokenizer as ApacheTokenizer

/**
 * A [PipelineStep] that splits the input into its constituent tokens
 *
 * This tokenizer splits the input on whitespace. Based on the Apache Open NLP [WhitespaceTokenizer].
 */
public object Tokenizer : PipelineStep {
    private val tokenizer: ApacheTokenizer = WhitespaceTokenizer.INSTANCE

    public fun process(input: String): List<String> {
        return tokenizer.tokenize(input).toList()
    }

    override fun process(input: List<String>): List<String> {
        val allTokens = mutableListOf<String>()
        for (i in input) {
            val tokens = tokenizer.tokenize(i)
            allTokens.addAll(tokens)
        }
        return allTokens
    }
}
