package com.haroldadmin.lucilla.ir

/**
 * A Posting records information about a token in a document's property.
 *
 * A posting is specific to every token in every property of every document.
 * It's fundamental unit of information in an FTS index. It stores the following
 * info:
 * - ID of the document
 * - Length of the property of the document
 * - Offsets of the token in property's tokens. The number of positions
 * is also its term frequency in the document.
 */
public data class Posting(
    val docId: Int,
    val property: String,
    val propertyLength: Int,
    val offsets: List<Int>,
)
