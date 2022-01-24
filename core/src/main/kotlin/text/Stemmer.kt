package com.haroldadmin.lucilla.core.text

import com.haroldadmin.lucilla.core.PipelineStep
import opennlp.tools.stemmer.PorterStemmer as ApachePorterStemmer
import opennlp.tools.stemmer.snowball.SnowballStemmer as ApacheSnowballStemmer
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM as SnowballStemmerAlgorithm

/**
 * A [PipelineStep] that stems the input text using the Porter Stemmer
 * Algorithm.
 *
 * Based on the Apache Open NLP [ApachePorterStemmer].
 */
public object PorterStemmer : PipelineStep {
    private val stemmer = ApachePorterStemmer()

    override fun process(input: List<String>): List<String> {
        val stems = mutableListOf<String>()
        for (i in input) {
            stems.add(process(i))
        }
        return stems
    }

    public fun process(input: String): String {
        val stem = stemmer.stem(input)
        stemmer.reset()
        return stem
    }
}

/**
 * A [PipelineStep] that stems the input text using the Snowball
 * stemming algorithm.
 *
 * Based on the Apache Open NLP [ApacheSnowballStemmer]
 */
public class SnowballStemmer(
    algorithm: SnowballStemmerAlgorithm = SnowballStemmerAlgorithm.ENGLISH,
    repeat: Int = 1
) : PipelineStep {
    private val stemmer = ApacheSnowballStemmer(algorithm, repeat)

    override fun process(input: List<String>): List<String> {
        return input.map { i -> stemmer.stem(i).toString() }.toList()
    }
}
