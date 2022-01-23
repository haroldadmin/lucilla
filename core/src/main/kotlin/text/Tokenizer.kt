package com.haroldadmin.lucilla.core.text

import com.haroldadmin.lucilla.core.PipelineStep
import opennlp.tools.tokenize.Tokenizer as ApacheTokenizer
import opennlp.tools.tokenize.WhitespaceTokenizer

public object WhitespaceTokenizer : PipelineStep {
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