package com.haroldadmin.lucilla.core

/**
 * A autocompletion suggestion for a search query
 */
public data class AutocompleteSuggestion(
    /**
     * Score of the autocompletion suggestion.
     * Higher score implies more relevance.
     */
    val score: Double,

    /**
     * The autocompletion suggestion
     */
    val suggestion: String,
)
