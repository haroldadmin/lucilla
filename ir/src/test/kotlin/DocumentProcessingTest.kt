package com.haroldadmin.lucilla.ir

import com.haroldadmin.lucilla.annotations.Id
import com.haroldadmin.lucilla.annotations.Ignore
import com.haroldadmin.lucilla.pipeline.Pipeline
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe

class DocumentProcessingTest : DescribeSpec({
    describe("extractDocumentId") {
        it("should extract ID from a document with @Id prop") {
            data class Doc(
                @Id
                val Id: Int,
            )

            val id = extractDocumentId(Doc(42))
            id shouldBe 42
        }

        it("should extract ID from a document with @Id method") {
            class Doc(private val id: Int) {
                @Id
                fun id(): Int = this.id
            }

            val id = extractDocumentId(Doc(42))
            id shouldBe 42
        }

        it("should throw an error if document has no @Id fields") {
            class Doc
            shouldThrow<IllegalArgumentException> {
                extractDocumentId(Doc())
            }
        }

        it("should throw an error if document has multiple ID fields") {
            class Doc(
                @Id val id: Int
            ) {
                @Id
                fun id(): Int = this.id
            }

            shouldThrow<IllegalArgumentException> {
                extractDocumentId(Doc(42))
            }
        }

        it("should throw an error if ID member does not return an Int") {
            class Doc(@Id val id: String)

            shouldThrow<IllegalArgumentException> {
                extractDocumentId(Doc("foo"))
            }
        }

        it("should throw an error if ID member returns null") {
            class Doc(val id: Int?)

            shouldThrow<IllegalArgumentException> {
                extractDocumentId(Doc(null))
            }
        }
    }

    describe("extractProperties") {
        it("should extract all relevant properties from a document") {
            data class Doc(val name: String, val author: String)

            val doc = Doc("Foo", "Bar")
            val props = extractProperties(doc).map { prop -> prop.call(doc) }
            props shouldContainExactlyInAnyOrder listOf("Foo", "Bar")
        }

        it("should ignore properties marked with @Ignore") {
            data class Doc(val name: String, @Ignore val author: String)

            val doc = Doc("Foo", "Bar")
            val props = extractProperties(doc).map { prop -> prop.call(doc) }
            props shouldContainExactly listOf("Foo")
        }

        it("should ignore properties that are not Strings") {
            data class Doc(val name: String, val year: Int)

            val doc = Doc("Foo", 2022)
            val props = extractProperties(doc).map { prop -> prop.call(doc) }
            props shouldContainExactly listOf("Foo")
        }

        it("should ignore member functions") {
            data class Doc(val name: String, val author: String) {
                @Suppress("unused")
                fun formattedName() = "$name $author"
            }

            val doc = Doc("Foo", "Bar")
            val props = extractProperties(doc).map { prop -> prop.call(doc) }
            props shouldContainExactlyInAnyOrder listOf("Foo", "Bar")
        }

        it("should ignore null properties") {
            data class Doc(val name: String?)

            val doc = Doc(null)
            val props = extractProperties(doc)
            props shouldHaveSize 0
        }

        it("should ignore properties marked with @Id") {
            data class Doc(@Id val id: String, val name: String)

            val doc = Doc("Foo", "Bar")
            val props = extractProperties(doc).map { prop -> prop.call(doc) }
            props shouldContainExactly listOf("Bar")
        }
    }

    describe("extractTokens") {
        it("should return tokens from the pipeline") {
            val pipeline = Pipeline.of({ input -> input })
            val tokens = extractTokens("El Plan", pipeline)
            tokens shouldContainExactly listOf("El Plan")
        }
    }

    describe("extractPostings") {
        val pipeline = Pipeline.of({ input ->
            input.map { it.split(" ") }.flatten()
        })

        it("should return an empty postings for doc with no strings") {
            // No text properties
            data class Doc(@Id val id: Int)

            val postings = extractPostings(Doc(0), pipeline)
            postings shouldHaveSize 0
        }

        it("should return postings for all string properties of the document") {
            data class Doc(@Id val id: Int, val size: Int, val title: String)

            val postings = extractPostings(Doc(0, 0, "Zero"), pipeline)

            val props = postings.map { (_, posting) -> posting.property }.distinct()
            props shouldContainExactly listOf("title")
        }

        it("should return a posting for each token") {
            data class Doc(@Id val id: Int, val title: String)

            val doc = Doc(0, "how to build a car")
            val postings = extractPostings(doc, pipeline)

            val expectedTokens = pipeline.process(doc.title).distinct()
            postings shouldHaveSize expectedTokens.size

            for (expectedToken in expectedTokens) {
                postings shouldContainKey expectedToken
            }
        }

        it("should return the correct posting for each token") {
            data class Doc(@Id val id: Int, val title: String)

            val doc = Doc(0, "how to build a car")
            val postings = extractPostings(doc, pipeline)

            postings["how"]?.docId shouldBe doc.id
            postings["how"]?.offsets shouldBe listOf(0)
        }
    }

    describe("calculateTokenPostings") {
        val pipeline = Pipeline.of({ input -> input.map { it.split(" ") }.flatten() })

        it("should return empty postings for empty input") {
            val postings = calculateTokenPostings(0, "foo", emptyList())
            postings shouldHaveSize 0
        }

        it("should return correct postings for each token") {
            val string = "a day late and a dollar short"
            val tokens = pipeline.process(string)
            val postings = calculateTokenPostings(0, "foo", tokens)

            postings shouldHaveSize tokens.distinct().size
            for (token in tokens) {
                val expectedCount = tokens.count { it == token }
                postings[token]?.offsets?.size shouldBe expectedCount
                postings[token]?.propertyLength shouldBe tokens.size
            }
        }
    }
})
