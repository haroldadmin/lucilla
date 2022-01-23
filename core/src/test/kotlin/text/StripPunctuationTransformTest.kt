package com.haroldadmin.lucilla.core.text

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class StripPunctuationTransformTest : DescribeSpec({
    it("should remove punctuation from input text") {
        StripPunctuationTransform.process("Michael, have you got a minute?") shouldBe "Michael have you got a minute"
    }
})
