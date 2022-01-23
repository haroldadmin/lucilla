package com.haroldadmin.lucilla.core.text

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class StemmerTest : DescribeSpec({
    describe(PorterStemmer::class.java.simpleName) {
        it("should stem input text") {
            PorterStemmer.process("moves") shouldBe PorterStemmer.process("move")
        }
    }
})