package com.haroldadmin.lucilla.core

import com.haroldadmin.lucilla.annotations.Id
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe

class FtsIndexTest : DescribeSpec({
    describe(FtsIndex::class.java.simpleName) {
        it("should report size 0 if document is empty") {
            val fts = useFts<Sentence>()
            fts.size shouldBe 0
        }

        it("should add a document to the index successfully") {
            val fts = useFts<Sentence>()
            val (sentence) = generateSentences().take(1).toList()
            fts.add(sentence)
            fts.size shouldBe 1
        }

        it("should add a document's tokens to the index") {
            val fts = useFts<Sentence>()
            val (sentence) = generateSentences().take(1).toList()
            fts.add(sentence)

            val tokens = fts.pipeline.process(sentence.value).toSet()
            fts.index.size shouldBe tokens.size
        }

        it("should remove a document from the index successfully") {
            val fts = useFts<Sentence>()
            val (sentence) = generateSentences().take(1).toList()
            fts.add(sentence)
            fts.remove(sentence)
            fts.size shouldBe 0
        }

        it("should remove a document's tokens from the index") {
            val fts = useFts<Sentence>()
            val (sentence) = generateSentences().take(1).toList()
            fts.apply {
                add(sentence)
                remove(sentence)
            }

            fts.index.size shouldBe 0
        }

        it("should not throw an error when removing a non-existent doc from the index") {
            val fts = useFts<Sentence>()
            val (sentence) = generateSentences().take(1).toList()
            fts.remove(sentence)
            fts.size shouldBe 0
        }

        it("should not add a document twice to the index") {
            val fts = useFts<Sentence>()
            val (sentence) = generateSentences().take(1).toList()
            fts.add(sentence)
            fts.add(sentence)

            fts.size shouldBe 1
        }

        it("should not index fields marked with @Ignore") {
            val fts = useFts<Book>()
            val book = Book(id = 0, title = "How to Build a Car", author = "Adrian Newey", publisher = "foo")
            fts.add(book)

            fts.index shouldNotContainKey book.publisher
        }

        it("should index documents with '@Id' annotated property") {
            val fts = useFts<Book>()
            val books = generateBooks().take(10).toList()
            books.forEach { fts.add(it) }

            val docIds = fts.index.values
                .flatMap { propDocs -> propDocs.values }
                .flatMap { docFreqs -> docFreqs.keys }
                .toSet()

            docIds shouldContainExactlyInAnyOrder books.map { b -> b.id }
        }

        it("should throw an error when adding a document with no '@Id' annotated element") {
            data class DocumentWithoutID(
                val title: String
            )

            val fts = useFts<DocumentWithoutID>()

            shouldThrow<IllegalArgumentException> {
                fts.add(DocumentWithoutID("Test"))
            }
        }

        it("should throw an error when adding a document with non-Int '@Id'") {
            data class DocumentWithStringID(
                @Id
                val id: String,
                val title: String
            )

            val fts = useFts<DocumentWithStringID>()

            shouldThrow<IllegalArgumentException> {
                fts.add(DocumentWithStringID("Test ID", "Test"))
            }
        }

        it("should return all documents for a token when searching") {
            val index = useFts<Book>()
            val books = generateBooks().toList().associateBy { book -> book.id }
            books.values.forEach { index.add(it) }

            val matchedBooks = index.search("pride")
                .mapNotNull { r -> books[r.documentId] }

            matchedBooks shouldHaveSize 2
            matchedBooks.all { it.author == "Jane Austen" } shouldBe true
        }

        it("should return empty set when there are no results") {
            val fts = useFts<Book>()
            fts.search("test") shouldHaveSize 0
        }
    }
})
