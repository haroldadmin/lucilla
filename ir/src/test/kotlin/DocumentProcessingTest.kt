package com.haroldadmin.lucilla.ir

import com.haroldadmin.lucilla.annotations.Id
import com.haroldadmin.lucilla.annotations.Ignore
import com.haroldadmin.lucilla.pipeline.Pipeline
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
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

            val props = extractProperties(Doc("Foo", "Bar"))
            props shouldContainExactlyInAnyOrder listOf("Foo", "Bar")
        }

        it("should ignore properties marked with @Ignore") {
            data class Doc(val name: String, @Ignore val author: String)

            val props = extractProperties(Doc("Foo", "Bar"))
            props shouldContainExactly listOf("Foo")
        }

        it("should ignore properties that are not Strings") {
            data class Doc(val name: String, val year: Int)

            val props = extractProperties(Doc("Foo", 2022))
            props shouldContainExactly listOf("Foo")
        }

        it("should ignore member functions") {
            data class Doc(val name: String, val author: String) {
                fun formattedName() = "$name $author"
            }

            val props = extractProperties(Doc("Foo", "Bar"))
            props shouldContainExactlyInAnyOrder listOf("Foo", "Bar")
        }

        it("should ignore null properties") {
            data class Doc(val name: String?)

            val props = extractProperties(Doc(null))
            props shouldHaveSize 0
        }

        it("should ignore properties marked with @Id") {
            data class Doc(@Id val id: String, val name: String)

            val props = extractProperties(Doc("Foo", "Bar"))
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
})
