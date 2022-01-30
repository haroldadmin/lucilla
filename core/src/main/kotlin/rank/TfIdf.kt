package com.haroldadmin.lucilla.core.rank

import kotlin.math.ln

/**
 * Calculates the TF-IDF value for a term.
 *
 * This method uses the logarithmic IDF variant.
 * The denominator is adjusted to be (1 + df) to avoid division
 * by zero errors.
 *
 * @param tf The term frequency in a document
 * @param df The number of documents in which the term appears
 * @param n The number of documents in the index
 * @return TF-IDF value for the term
 */
internal fun tfIdf(tf: Double, df: Int, n: Int): Double {
    return tf * (ln(n.toDouble() / (1 + df)))
}

/**
 * Calculates the TF-IDF value for a term.
 *
 * This method uses the logarithmic IDF variant.
 * The denominator is adjusted to be (1 + df) to avoid division
 * by zero errors.
 *
 * @param tf The term frequency in a document
 * @param df The number of documents in which the term appears
 * @param n The number of documents in the index
 * @return TF-IDF value for the term
 */
internal fun tfIdf(tf: Int, df: Int, n: Int): Double {
    return tf * (ln(n.toDouble() / (1 + df)))
}
