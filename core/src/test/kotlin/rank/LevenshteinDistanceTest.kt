package com.haroldadmin.lucilla.core.rank

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class LevenshteinDistanceTest : DescribeSpec({
    it("should return 0 if the strings are empty") {
        val distance = ld("", "")
        distance shouldBe 0
    }

    it("should return 0 if the strings are the same") {
        val distance = ld("foo", "foo")
        distance shouldBe 0
    }

    it("should return the correct distance when one of the strings is empty") {
        val distance = ld("foo", "")
        distance shouldBe 3
    }

    it("should return the correct distance when both strings are non-empty") {
        val distance = ld("ephrem", "benyam")
        distance shouldBe 5
    }
})
