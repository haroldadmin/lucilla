package com.haroldadmin.lucilla.core

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SearchQueryTest : DescribeSpec({
    describe("buildQuery") {
        it("should build search query with given text") {
            val query = buildQuery("foo")
            query.text shouldBe "foo"
        }

        it("should pass null selected props by default") {
            val query = buildQuery("foo")
            query.selectProps shouldBe null
        }

        it("should pass given selected props") {
            val query = buildQuery("foo") { select(Book::title) }
            query.selectProps shouldBe listOf(Book::title.name)
        }

        it("should pass empty selected props if none are specified") {
            val query = buildQuery("foo") { select() }
            query.selectProps shouldBe emptyList<String>()
        }
    }
})
