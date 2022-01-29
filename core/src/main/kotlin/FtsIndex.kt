package com.haroldadmin.lucilla.core

import com.haroldadmin.lucilla.core.rank.documentFrequency
import com.haroldadmin.lucilla.core.rank.termFrequency
import com.haroldadmin.lucilla.core.rank.tfIdf
import com.haroldadmin.lucilla.ir.extractDocumentId
import com.haroldadmin.lucilla.ir.extractProperties
import com.haroldadmin.lucilla.ir.extractTokens
import com.haroldadmin.lucilla.pipeline.Pipeline
import org.apache.commons.collections4.Trie
import org.apache.commons.collections4.trie.PatriciaTrie

/**
 * Result of a search query
 */
public data class SearchResult(
    /**
     * ID of the matched document
     */
    val documentId: Int,

    /**
     * Score of the match. Higher score implies a better
     * match.
     */
    val score: Double,

    /**
     * The fragment of the search query that matched against
     * this search result
     */
    val matchTerm: String,
)

/**
 * Alias for [Map] that models a token's frequency of appearance in documents
 *
 * Key = Document ID
 * Value = Frequency of the token in the document
 */
internal typealias DocumentFrequencies = MutableMap<Int, Int>

/**
 * Alias for a token in a document
 */
internal typealias Token = String

/**
 * Alias for a [Map] that maps a document token against frequencies of that token
 * in all documents according to each property of the document
 *
 * Example:
 * ```kotlin
 * data class Doc(val name: String, val author: String)
 * ```
 * can be visualised as:
 *
 * ```json
 * {
 *   "<some-token>": {
 *     "name": {
 *       "<doc-id>": "<frequency>",
 *     },
 *     "author": {
 *       "<doc-id>": "<frequency>"
 *     }
 *   }
 * }
 * ```
 * ```
 */
internal typealias InvertedIndex = Map<Token, Map<String, DocumentFrequencies>>

/**
 * Mutable variant of [InvertedIndex].
 *
 * Backed by a [PatriciaTrie] for efficient space utilisation.
 */
internal typealias MutableInvertedIndex = Trie<Token, MutableMap<String, DocumentFrequencies>>

/**
 * A Full Text Search index for fast and efficient
 * information retrieval.
 *
 * - Add documents to the index using [add], and search using [search].
 * - To customise the text processing rules, create a custom [Pipeline] and
 * pass it as the [pipeline] parameter
 */
public class FtsIndex<DocType : Any>(
    public val pipeline: Pipeline
) {
    /**
     * FTS index modeled as a PATRICIA trie
     *
     * The keys in the trie are tokens extracted from documents in the index
     * The values are document frequencies of the token in all documents of the index
     */
    private val _index: MutableInvertedIndex = PatriciaTrie()

    /**
     * Public read-only view of the internal FTS index
     */
    public val index: InvertedIndex
        get() = _index

    /**
     * Set of documents present in the index, along with the number of tokens in
     * each doc.
     *
     * We store the token length of a document for calculating match scores.
     * If a search term occurs in two documents just once, both of them will get the
     * same score. To distinguish between the quality of match between the two,
     * we divide their score with the number of tokens in each doc.
     */
    private val docs: MutableMap<Int, Int> = mutableMapOf()

    /**
     * Number of documents in the index
     */
    public val size: Int
        get() = docs.size

    /**
     * Number of tokens in the index
     */
    public val tokenCount: Int
        get() = _index.size

    /**
     * Adds the given document to the FTS index.
     *
     * A document consists of multiple fields, each of which contributes to index.
     * This method adds the document to the index as follows:
     *
     * 1. Extracts all member properties of the documents and filters the ones marked with
     * [com.haroldadmin.lucilla.annotations.Ignore] and [com.haroldadmin.lucilla.annotations.Id]
     * 2. Converts each extracted property to its value as a string
     * 3. Runs the string value through the Text Processing [Pipeline] to extract tokens
     * from the document.
     * 4. Add each token as a key to the index, with the value set to its property
     * specific document frequencies
     *
     * Returns without modifying the index if a document with the given ID is already
     * present in the index.
     *
     * @param doc The document to add to the index
     * @return The number of tokens added to the index from the document
     */
    public fun add(doc: DocType): Int {
        val docId = extractDocumentId(doc)
        if (docId in docs) {
            return 0
        }

        val docProps = extractProperties(doc)
        val propsToTokens = docProps.associate { prop ->
            val propValue = prop.call(doc)
            val propTokens = extractTokens(propValue, pipeline)
            prop.name to propTokens
        }

        for ((prop, tokens) in propsToTokens) {
            for (token in tokens) {
                val propsForToken = _index[token] ?: mutableMapOf()
                val docFrequenciesForProp = propsForToken[prop] ?: mutableMapOf()
                val tokenFrequency = docFrequenciesForProp.getOrDefault(docId, 0) + 1

                docFrequenciesForProp[docId] = tokenFrequency
                propsForToken[prop] = docFrequenciesForProp
                _index[token] = propsForToken
            }
        }

        val docLength = propsToTokens.values.sumOf { tokens -> tokens.size }
        docs[docId] = docLength

        return propsToTokens.size
    }

    /**
     * Removes the given document from the index.
     *
     * To remove a document we must remove its ID from the set of IDs
     * stored with every token in the index. To do this we must
     * extract all the tokens from the document, and remove the document's ID
     * from each one.
     *
     * If a token maps to an empty set of IDs after removing the document's ID
     * from it, it is removed from the index entirely.
     *
     * @param doc The document to remove from the index
     */
    public fun remove(doc: DocType) {
        val docId = extractDocumentId(doc)
        if (docId !in docs) {
            return
        }

        val docProps = extractProperties(doc)
        val docTokens = docProps
            .map { prop ->
                val propValue = prop.call(doc)
                extractTokens(propValue, pipeline)
            }
            .flatten()

        for (token in docTokens) {
            val tokenProps = _index[token]
            if (tokenProps == null || tokenProps.isEmpty()) {
                _index.remove(token)
                continue
            }

            val propsToRemove = mutableListOf<String>()
            for ((prop, documentFrequencies) in tokenProps) {
                val existingFrequency = documentFrequencies.getOrDefault(docId, 0)
                val newFrequency = existingFrequency - 1
                documentFrequencies[docId] = newFrequency

                if (newFrequency < 1) {
                    documentFrequencies.remove(docId)
                }

                if (documentFrequencies.isEmpty()) {
                    propsToRemove.add(prop)
                }
            }

            for (prop in propsToRemove) {
                tokenProps.remove(prop)
            }

            if (tokenProps.isEmpty()) {
                _index.remove(token)
            }
        }

        docs.remove(docId)
    }

    /**
     * Searches the FTS index for exact matches of the given query
     * and the tokens extracted from it. Returns the matching results
     * in order of relevance.
     *
     * @param query The query to search for
     * @return Search results for the query ordered by relevance
     */
    public fun search(query: String): List<SearchResult> {
        if (query.isBlank()) {
            return emptyList()
        }

        val queryTokens = mutableSetOf(query)
        queryTokens.addAll(pipeline.process(query))

        val results = mutableListOf<SearchResult>()
        for (queryToken in queryTokens) {
            val matchingProps = _index[queryToken] ?: continue
            for ((prop, docFrequencies) in matchingProps) {
                for (docId in docFrequencies.keys) {
                    val score = score(queryToken, docId, prop)
                    val result = SearchResult(docId, score, queryToken)
                    results.add(result)
                }
            }
        }

        results.sortByDescending { result -> result.score }
        return results
    }

    /**
     * Clears all documents added to the FTS index
     */
    public fun clear() {
        this.docs.clear()
        this._index.clear()
    }

    /**
     * Calculates the match score for a term in a document using the
     * TF-IDF algorithm
     *
     * @param term The term whose TF-IDF value must be calculated
     * @param docId The document in which the term appears
     * @return The TF-IDF value for the term in the document
     */
    private fun score(term: String, docId: Int, prop: String): Double {
        val docLength = docs[docId] ?: 0
        val tf = termFrequency(term, docId, docLength, _index, prop)
        val df = documentFrequency(term, _index, prop)
        val n = docs.size
        return tfIdf(tf, df, n)
    }
}

/**
 * Creates an FTS index
 *
 * @param T The type of documents to be stored in the index
 * @param docs Seed data to add to the index
 * @param pipeline A custom text processing pipeline, if any
 * @return An FTS index with the given pipeline and seed data (if any)
 */
public fun <T : Any> useFts(
    docs: List<T>? = null,
    pipeline: Pipeline = Pipeline.Default
): FtsIndex<T> {
    val index = FtsIndex<T>(pipeline)
    docs?.forEach { index.add(it) }

    return index
}
