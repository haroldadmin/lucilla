package com.haroldadmin.lucilla.core

import kotlin.system.measureTimeMillis

fun main() {
    val index = useFts<Book>()

    println("index:start")
    val books = generateBooks().toList().associateBy { it.id }
    val timeToBuildIndex = measureTimeMillis {
        books.values.forEach { index.add(it) }
    }
    println("index:complete ($timeToBuildIndex ms)")

    println("s: search, a: autocompletion suggestions")
    when (readln().trim().lowercase()) {
        "s" -> search(index, books)
        "a" -> autocomplete(index, books)
        else -> println("Invalid input")
    }
}

fun search(index: FtsIndex<Book>, data: Map<Int, Book>) {
    println("Search")
    println("Enter search query (EXIT to stop)")
    while (true) {
        val query = readln()
        if (query == "EXIT") {
            break
        }

        println("Searching for '$query'")
        val results: List<SearchResult>
        val searchTime = measureTimeMillis { results = index.search(query) }
        println("${results.size} results, $searchTime ms")

        results.forEachIndexed { i, result ->
            val book = data[result.documentId]!!
            println("$i\t(${result.score}, ${result.matchTerm})\t${book.title}, ${book.author}")
        }
    }
}

fun autocomplete(index: FtsIndex<Book>, data: Map<Int, Book>) {
    println("Autocomplete Suggestions")
    println("Enter search query (EXIT TO STOP)")
    while (true) {
        val query = readln()
        if (query == "EXIT") {
            break
        }

        println("Suggestions for '$query'")
        val suggestions: List<AutocompleteSuggestion>
        val searchTime = measureTimeMillis { suggestions = index.autocomplete(query) }
        println("($searchTime ms) ${suggestions.joinToString { it.suggestion }}")
    }
}
