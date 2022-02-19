package com.haroldadmin.lucilla.core

import com.haroldadmin.lucilla.core.rank.ld
import com.haroldadmin.lucilla.core.rank.tfIdf
import com.haroldadmin.lucilla.ir.Posting
import com.haroldadmin.lucilla.ir.extractDocumentId
import com.haroldadmin.lucilla.ir.extractPostings
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

public data class AutocompleteSuggestion(
    val score: Double,
    val suggestion: String,
)

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
 *     "name": [
 *       { "<doc-id>": "<offsets>", "<length>" }
 *     ],
 *     "author": [
 *       { "<doc-id>": "<offsets>", "<length>" }
 *     ]
 *   }
 * }
 * ```
 * ```
 */
internal typealias InvertedIndex = Map<Token, List<Posting>>

/**
 * Mutable variant of [InvertedIndex].
 *
 * Backed by a [PatriciaTrie] for efficient space utilisation.
 */
internal typealias MutableInvertedIndex = Trie<Token, MutableList<Posting>>

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
     * Map of all documents present in the index, along with the token lengths of
     * each document's properties.
     *
     * We store the token length of a document's properties for calculating match scores.
     * If a search term occurs in two documents just once, both of them will get the
     * same score. To distinguish between the quality of match between the two,
     * we divide their score with the number of tokens in each doc's properties.
     */
    private val _docs: MutableSet<Int> = mutableSetOf()

    /**
     * Map of the average lengths of each property in [DocType]
     */
    private val averageLengths: MutableMap<String, Int> = mutableMapOf()

    /**
     * Public read-only view of the internal docs list
     */
    public val docs: Set<Int>
        get() = _docs

    /**
     * Number of documents in the index
     */
    public val size: Int
        get() = _docs.size

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
     * 4. Calculate the [Posting] list for each token, and add it to the index
     *
     * Returns without modifying the index if a document with the given ID is already
     * present in the index.
     *
     * @param doc The document to add to the index
     * @return The number of tokens added to the index from the document
     */
    public fun add(doc: DocType): Int {
        val docId = extractDocumentId(doc)
        if (docId in _docs) {
            return 0
        }
        _docs.add(docId)

        val docProperties = extractProperties(doc)
        val tokensToPostings = extractPostings(doc, pipeline, docId, docProperties)

        var addedTokens = 0
        for ((token, posting) in tokensToPostings) {
            val postingList = _index[token] ?: mutableListOf<Posting>().also { addedTokens++ }
            postingList.add(posting)
            _index[token] = postingList

            val property = posting.property
            val totalPropLength = averageLengths.getOrDefault(property, 0) * docs.size
            val averagePropLength = totalPropLength / docs.size
            averageLengths[property] = averagePropLength
        }

        return addedTokens
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
        if (docId !in _docs) {
            return
        }
        _docs.remove(docId)

        val docTokens = extractProperties(doc)
            .map { prop -> extractTokens(prop.call(doc), pipeline) }
            .flatten()

        for (token in docTokens) {
            val postingList = _index[token]
            if (postingList == null || postingList.isEmpty()) {
                _index.remove(token)
                continue
            }

            postingList.removeIf { it.docId == docId }

            if (postingList.isEmpty()) {
                _index.remove(token)
            }
        }
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
            val postingList = _index[queryToken] ?: continue
            val postingListToDocs = postingList.groupBy { it.docId }
            for ((docId, docPostingList) in postingListToDocs) {
                val score = score(docPostingList)
                val result = SearchResult(docId, score, queryToken)
                results.add(result)
            }
        }

        results.sortBy { result -> result.score }
        return results
    }

    /**
     * Fetches autocompletion suggestions for the given query.
     *
     * An autocompletion suggestion is a term present in the index that
     * has the same prefix as the given search query. The results are sorted
     * in order of their relevance score.
     * e.g. "foo" -> "fool", "foot", "football"
     *
     * **Autocompletion suggestions can be unexpected if stemming is a part
     * of your text processing pipeline.**
     *
     * For example, the Porter stemmer stems "football" to "footbal". Therefore,
     * even if your input text contains the word "football", you will see "footbal"
     * as an autocompletion suggestion instead.
     *
     * The simplest way around this is to use a [Pipeline] that does not contain
     * a stemming step. Alternatively you can use a custom stemmer that emits
     * both the original word and its stemmed variant to ensure the original
     * word appears in the suggestions.
     *
     * *Expect the autocompletion ranking algorithm to change in future releases*
     *
     * @param query The search query to fetch autocompletion suggestions for
     * @return List of autocompletion suggestions, sorted by their scores
     */
    public fun autocomplete(query: String): List<AutocompleteSuggestion> {
        val suggestions = _index.prefixMap(query).keys
            .fold(mutableListOf<AutocompleteSuggestion>()) { suggestions, prefixKey ->
                val score = ld(query, prefixKey).toDouble() / prefixKey.length
                val suggestion = AutocompleteSuggestion(score, prefixKey)
                suggestions.apply { add(suggestion) }
            }

        suggestions.sortByDescending { it.score }

        return suggestions
    }

    /**
     * Clears all documents added to the FTS index
     */
    public fun clear() {
        this._docs.clear()
        this._index.clear()
    }

    /**
     * Calculates the match score for a term in a document using the
     * TF-IDF algorithm.
     *
     * The given postings must all be for the same document.
     *
     * @param postingList The posting list for the term for a specific document
     * @return The TF-IDF value for the term in the document
     */
    private fun score(postingList: List<Posting>): Double {
        val df = postingList.size
        val n = _docs.size

        var tf = 0
        var propLength = 0
        var averageLength = 0
        for (posting in postingList) {
            tf += posting.offsets.size
            propLength += posting.propertyLength
            averageLength += averageLengths.getOrDefault(posting.property, 0)
        }

        val normalizationFactor = propLength / (1 + averageLength)
        return tfIdf(tf, df, n) / normalizationFactor
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
