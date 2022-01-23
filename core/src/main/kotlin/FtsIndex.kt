package com.haroldadmin.lucilla.core

import com.haroldadmin.lucilla.core.rank.documentFrequency
import com.haroldadmin.lucilla.core.rank.termFrequency
import com.haroldadmin.lucilla.core.rank.tfIdf
import org.apache.commons.collections4.Trie
import org.apache.commons.collections4.trie.PatriciaTrie
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

public data class SearchResult(
    val documentId: Int,
    val score: Double,
    val matchTerm: String,
)

internal typealias DocumentFrequencies = MutableMap<Int, Int>
internal typealias Token = String
internal typealias InvertedIndex = Map<Token, DocumentFrequencies>
internal typealias MutableInvertedIndex = Trie<Token, DocumentFrequencies>

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
     * [Ignore] and [Id]
     * 2. Converts each extracted property to its value as a string
     * 3. Runs the string value through the Text Processing [Pipeline] to extract tokens
     * from the document.
     * 4. Add each token as a key to the index, with the value set to its document frequencies
     *
     * Returns without modifying the index if a document with the given ID is already
     * present in the index.
     *
     * @param doc The document to add to the index
     * @return The number of tokens added to the index from the document
     */
    public fun add(doc: DocType): Int {
        val docId = extractDocId(doc)
        if (docId in docs) {
            return 0
        }

        val docProps = extractProperties(doc)
        val docTokens = extractTokens(docProps)
        docTokens.forEach { token ->
            val docFrequencies = _index[token] ?: mutableMapOf()
            val tokenFrequency = docFrequencies.getOrDefault(docId, 0) + 1
            docFrequencies[docId] = tokenFrequency

            _index[token] = docFrequencies
        }

        val docLength = docTokens.size
        docs[docId] = docLength

        return docTokens.size
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
        val docId = extractDocId(doc)
        if (docId !in docs) {
            return
        }

        val docProps = extractProperties(doc)
        val docTokens = extractTokens(docProps)

        for (token in docTokens) {
            val docFrequencies = _index[token]
            if (docFrequencies == null || docFrequencies.isEmpty()) {
                _index.remove(token)
                continue
            }

            val existingFrequency = docFrequencies.getOrDefault(docId, 0)
            val newFrequency = existingFrequency - 1
            docFrequencies[docId] = newFrequency

            if (newFrequency < 1) {
                docFrequencies.remove(docId)
            }

            if (docFrequencies.isEmpty()) {
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
            val matchingDocs = _index[queryToken] ?: continue
            for (docId in matchingDocs.keys) {
                val score = score(queryToken, docId)
                val result = SearchResult(docId, score, queryToken)
                results.add(result)
            }
        }

        results.sortByDescending { result -> result.score }
        return results
    }

    /**
     * Extract the ID from the document's properties.
     *
     * The document MUST have an [Id] annotated property/method that
     * returns an [Int].
     *
     * Example:
     * ```kt
     * data class Document(
     *   @Id
     *   val id: Int,
     *   val name: String
     * )
     *
     * val doc = Document(1, "First")
     * extractDocId(doc) // 1
     * ```
     */
    private fun extractDocId(doc: DocType): Int {
        val idCallable = doc::class.members.find { callable -> callable.hasAnnotation<Id>() }

        requireNotNull(idCallable) { "$doc does not have any property/method annotated with '@Id'" }
        require(idCallable.returnType.isSubtypeOf(typeOf<Int>())) { "'@Id' annotated property/method must return an Int" }

        return idCallable.call(doc) as Int
    }

    /**
     * Extracts the values of the given document's properties that contribute
     * to the FTS index.
     *
     * Example:
     * ```kt
     * data class Document(
     *   val title: String,
     *   @Ignore
     *   val abstract: String,
     *   val author: String,
     * )
     *
     * val doc = Document("Foo", "bar", "baz)
     * getDocProperties(doc) // ["Foo", "baz"]
     * ```
     *
     * @param doc The document to extract property values from
     * @return List of the document's property values
     */
    private fun extractProperties(doc: DocType): List<String> {
        return doc::class.declaredMemberProperties
            .filterNot { prop -> prop.hasAnnotation<Ignore>() || prop.hasAnnotation<Id>() }
            .mapNotNull { prop -> prop.getter.call(doc)?.toString() }
    }

    /**
     * Extracts a [Set] of tokens from the given list of Strings.
     *
     * Every given string is passed through the text processing pipeline
     * to produce a list of tokens. All such tokens are merged into a single
     * set to remove duplicates.
     *
     * @param values The list of strings to extract tokens from
     * @return A set of unique tokens extracted from the given strings
     */
    private fun extractTokens(values: List<String>): Set<String> {
        return values
            .map { value -> pipeline.process(value) }
            .flatten()
            .toSet()
    }

    /**
     * Calculates the match score for a term in a document using the
     * TF-IDF algorithm
     *
     * @param term The term whose TF-IDF value must be calculated
     * @param docId The document in which the term appears
     * @return The TF-IDF value for the term in the document
     */
    private fun score(term: String, docId: Int): Double {
        val docLength = docs[docId] ?: 0
        val tf = termFrequency(term, docId, docLength, _index)
        val df = documentFrequency(term, _index)
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
