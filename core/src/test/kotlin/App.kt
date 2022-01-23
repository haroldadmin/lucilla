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

    do {
        println("Enter search query (EXIT to stop)")
        val query = readln()
        println("Searching for '$query'")
        var results: List<SearchResult>
        val searchTime = measureTimeMillis { results = index.search(query) }
        println("${results.size} results, $searchTime ms")

        results.forEachIndexed { i, result ->
            val book = books[result.documentId]!!
            println("$i\t(${result.score}, ${result.matchTerm})\t${book.title}, ${book.author}")
        }
    } while (query != "EXIT")
}