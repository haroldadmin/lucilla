package com.haroldadmin.lucilla.pipeline.transforms

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain

class LowercaseTransformTest : DescribeSpec({
    it("should lowercase text") {
        val text = "No Toto I don't access my emails during a race"
        LowercaseTransform.process(text) shouldContain "toto"
    }

    it("should lowercase all input texts") {
        val (first, second, third, fourth) = LowercaseTransform.process(
            listOf(
                "No Michael no! This is not right",
                "Toto, it's called a motor race",
                "Sorry?",
                "It's called car racing"
            )
        )

        first shouldContain "michael"
        second shouldContain "toto"
        third shouldContain "sorry"
        fourth shouldContain "it's"
    }
})
