package com.haroldadmin.lucilla.core

import java.io.File

data class Sentence(@Id val line: Int, val value: String)

internal fun generateSentences(): Sequence<Sentence> {
    val filePath = ClassLoader.getSystemResource("sentences.txt").path
    val file = File(filePath)
    return file.bufferedReader()
        .lineSequence()
        .mapIndexed { index, sentence -> Sentence(index, sentence) }
}

data class Book(
    @Id
    val id: Int,
    val title: String,
    val author: String,
    @Ignore
    val publisher: String,
)

internal fun generateBooks(): Sequence<Book> {
    val filePath = ClassLoader.getSystemResource("books.csv")!!.path
    val file = File(filePath)
    return file.bufferedReader()
        .lineSequence()
        .drop(1)
        .mapIndexedNotNull { index, bookData ->
            val (_, title, author, _, publisher) = bookData
                .split(";")
                .map { v -> v.substring(1 until v.length - 1) }
            Book(index, title, author, publisher)

        }
}