package com.haroldadmin.lucilla.core.rank

import com.haroldadmin.lucilla.core.InvertedIndex
import kotlin.math.ln

/**
 * Calculates the frequency of the term in the document with the given ID
 *
 * This method uses the "scaled frequency" variant of the term-frequency metric.
 * - Raw frequency is the number of times the term appears in the document.
 * - Scaled frequency adjusts for the raw frequency by dividing it with the length of the
 * document
 *
 * @param term The term to find the frequency of
 * @param docId The document to find the term's frequency in
 * @param docLength The length of the tokens in the document
 * @param index The FTS index
 * @return The scaled frequency of the term in the document
 */
internal fun termFrequency(
    term: String,
    docId: Int,
    docLength: Int,
    index: InvertedIndex,
): Double {
    val docsWithTerm = index[term] ?: return 0.0
    val rawTf = docsWithTerm[docId] ?: return 0.0
    return rawTf.toDouble() / docLength
}

/**
 * Returns the number of documents in which a term appears
 *
 * This method finds only the documents containing the exact term. Documents
 * that contain words with the given term as a prefix are not considered.
 *
 * @param term The term to find the document frequency of
 * @param index The FTS index
 * @return The number of documents in which the term appears
 */
internal fun documentFrequency(
    term: String,
    index: InvertedIndex,
): Int {
    val docsWithTerm = index[term] ?: return 0
    return docsWithTerm.keys.size
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
internal fun tfIdf(tf: Double, df: Int, n: Int): Double {
    return tf * (ln(n.toDouble() / (1 + df)))
}
