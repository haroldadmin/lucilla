package com.haroldadmin.lucilla.core

import kotlin.reflect.KProperty

/**
 * A search query with various options to customise how to
 * search the index and order results.
 */
public data class SearchQuery(
    val text: String,
    val selectProps: Set<String>? = null,
)

/**
 * Builder for [SearchQuery].
 *
 * Serves as the backing class for the DSL to build a search
 * query using [buildQuery].
 */
public class SearchQueryBuilder(
    private val text: String
) {
    /**
     * A null value indicates all indexed properties must be searched
     */
    private var selectProps: MutableSet<String>? = null

    /**
     * Specify which document properties to consider when searching
     * the index.
     *
     * Only the documents that match the search query within the selected
     * properties will be returned as a part of search results.
     *
     * The properties must be a part of the document type being indexed,
     * and must not be annotated with [com.haroldadmin.lucilla.annotations.Ignore].
     * Kotlin's type system is not flexible enough yet to restrict [props] to
     * a specific class's properties.
     *
     * @param props The properties to search within
     * @return The current builder instance
     */
    public fun select(vararg props: KProperty<String>): SearchQueryBuilder {
        val propNames = props.map { it.name }
        if (selectProps == null) {
            selectProps = propNames.toMutableSet()
        } else {
            propNames.forEach { selectProps?.add(it) }
        }
        return this
    }

    /**
     * Returns a finalized search query.
     *
     * @return Finalized search query
     */
    public fun build(): SearchQuery {
        return SearchQuery(text, selectProps)
    }
}

/**
 * DSL initializer for building a search query using [SearchQueryBuilder].
 *
 * @param text The search query text
 * @param queryBuilder Modifier for [SearchQueryBuilder]
 * @return Finalized search query
 */
public fun buildQuery(text: String, queryBuilder: SearchQueryBuilder.() -> Unit = {}): SearchQuery {
    return SearchQueryBuilder(text).apply(queryBuilder).build()
}
