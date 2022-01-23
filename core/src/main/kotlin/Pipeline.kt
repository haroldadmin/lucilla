package com.haroldadmin.lucilla.core

import com.haroldadmin.lucilla.core.text.LowercaseTransform
import com.haroldadmin.lucilla.core.text.PorterStemmer
import com.haroldadmin.lucilla.core.text.StripPunctuationTransform
import com.haroldadmin.lucilla.core.text.WhitespaceTokenizer

/**
 * A step in text processing [Pipeline] to ingest
 * a document's text content to produce tokens for the
 * FTS index.
 */
public fun interface PipelineStep {
    public fun process(input: List<String>): List<String>
}

/**
 * A text processing pipeline consisting of multiple [PipelineStep]s
 * that transform input text of a document into tokens for the
 * FTS index.
 *
 * The order of steps of the pipeline is important. Output of a
 * previous step becomes the input of the next step.
 */
public class Pipeline(
    private val steps: List<PipelineStep>
) {
    public companion object {
        /**
         * A pipeline consisting of sensible default [PipelineStep]s.
         *
         * This pipeline lower-cases the input text, strips punctuation,
         * splits text on whitespace, and stems every token.
         */
        public val Default: Pipeline = Pipeline(
            listOf(
                LowercaseTransform,
                StripPunctuationTransform,
                WhitespaceTokenizer,
                PorterStemmer
            )
        )

        /**
         * Convenience factory method to create a [Pipeline]
         * with the given steps.
         */
        public fun of(vararg steps: PipelineStep): Pipeline {
            return Pipeline(steps.toList())
        }
    }

    /**
     * Kicks off the text processing pipeline to produce
     * a list of tokens for the FTS index.
     */
    public fun process(input: String): List<String> {
        if (steps.isEmpty()) {
            return listOf(input)
        }

        var nextInput = listOf(input)
        for ((index, step) in steps.withIndex()) {
            try {
                nextInput = step.process(nextInput)
            } catch (e: Throwable) {
                throw Error("Text processing pipeline failed at step ${index + 1}", e)
            }
        }

        return nextInput
    }
}
