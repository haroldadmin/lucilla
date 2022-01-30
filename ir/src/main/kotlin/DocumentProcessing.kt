package com.haroldadmin.lucilla.ir

import com.haroldadmin.lucilla.annotations.Id
import com.haroldadmin.lucilla.annotations.Ignore
import com.haroldadmin.lucilla.pipeline.Pipeline
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

private val IntType = typeOf<Int>()
private val StringType = typeOf<String>()

/**
 * Extracts the value of the [Id] annotated member from a document
 *
 * - A member can be a property or a method.
 * - The document must contain one and only one [Id] annotated member
 * - The annotated member must return a non-null [Int] value
 *
 * @param T The document type
 * @param doc The document to extract the ID of
 * @return Value of the ID annotated property
 */
public fun <T : Any> extractDocumentId(doc: T): Int {
    val docName = doc::class.simpleName ?: "Document"
    val idProps = doc::class.declaredMembers.filter { member -> member.hasAnnotation<Id>() }

    require(idProps.isNotEmpty()) {
        "$docName does not contain any property/method annotated with @Id"
    }

    require(idProps.size == 1) {
        "$docName has multiple @Id annotated properties/methods"
    }

    val (idProp) = idProps
    require(idProp.returnType.isSubtypeOf(IntType)) {
        "$docName's @Id annotated property/method does not return an Int"
    }

    val id = idProp.call(doc) as? Int
    requireNotNull(id) {
        "$docName's @Id annotated property/method returned a null value"
    }

    return id
}

/**
 * Extracts the values of properties that return a [String] from the given document.
 *
 * - Only looks at properties in the class, not methods
 * - Ignores properties annotated with [Id] or [Ignore]
 * - Ignores properties that do not return a [String]
 * - Ignores properties that return a null value
 *
 * @param T The document type
 * @param doc The document to extract properties from
 * @return Values of extracted properties from the document
 */
public fun <T : Any> extractProperties(doc: T): List<KProperty<String>> {
    val stringProps = doc::class.declaredMemberProperties
        .filterNot { prop -> prop.hasAnnotation<Id>() || prop.hasAnnotation<Ignore>() }
        .filter { prop -> prop.returnType.isSubtypeOf(StringType) }

    @Suppress("UNCHECKED_CAST")
    return stringProps as List<KProperty<String>>
}

/**
 * Extracts tokens that can be indexed from the given data
 *
 * @param data The data to extract tokens from
 * @param pipeline The text processing pipeline to run this data through
 * @return Tokens extracted from the given data
 */
public fun extractTokens(data: String, pipeline: Pipeline): List<String> {
    return pipeline.process(data)
}

/**
 * Extracts the property-wise [Posting]s for each token in the given document.
 *
 * @param doc The document to extract token postings from
 * @param pipeline The text processing pipeline
 * @param docId ID of the document. Inferred from doc itself if not supplied
 * @param docProperties Properties of the document. Inferred from the doc itself if not
 * supplied
 * @return Map of document terms and their posting, keyed by the document properties
 */
public fun <T : Any> extractPostings(
    doc: T,
    pipeline: Pipeline,
    docId: Int = extractDocumentId(doc),
    docProperties: List<KProperty<String>> = extractProperties(doc),
): Map<String, Posting> {
    val result = mutableMapOf<String, Posting>()
    for (prop in docProperties) {
        val propName = prop.name
        val propValue = prop.call(doc)
        val propTokens = extractTokens(propValue, pipeline)

        val postings = calculateTokenPostings(docId, propName, propTokens)
        result.putAll(postings)
    }

    return result
}

/**
 * Calculates the [Posting] for each token in the given list.
 *
 * This function assumes that the list of tokens passed to it has not been
 * de-duplicated of repeated tokens.
 *
 * Example:
 * - Input:
 * ```
 * docId : 1
 * tokens : ["and", "its", "lights", "out", "and", "away", "we", "go"]
 * ```
 *
 * - Output:
 * ```json
 * {
 *   "and": {
 *     "docId": 1,
 *     "offsets": [0, 4],
 *   },
 *   "its": {
 *     "docId": 1,
 *     "offsets": [1],
 *    }
 *   ...
 * }
 * ```
 *
 * @param tokens The list of tokens to calculate postings for
 * @return Tokens mapped to their postings
 */
internal fun calculateTokenPostings(docId: Int, propName: String, tokens: List<String>): Map<String, Posting> {
    val tokenPositions = mutableMapOf<String, MutableList<Int>>()
    for ((index, token) in tokens.withIndex()) {
        val positions = tokenPositions[token] ?: mutableListOf()
        positions.add(index)
        tokenPositions[token] = positions
    }

    val postings = mutableMapOf<String, Posting>()
    for ((token, positions) in tokenPositions) {
        val posting = Posting(docId, propName, tokens.size, positions)
        postings[token] = posting
    }

    return postings
}
