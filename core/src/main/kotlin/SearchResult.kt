package com.haroldadmin.lucilla.core

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
