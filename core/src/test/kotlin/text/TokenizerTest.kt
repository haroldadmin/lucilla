package com.haroldadmin.lucilla.core.text

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TokenizerTest : DescribeSpec({
    it("should tokenize input") {
        val input = "Valtteri, it's James"
        Tokenizer.process(input) shouldBe input.split(" ")
    }
})
