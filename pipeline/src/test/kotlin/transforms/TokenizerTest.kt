package com.haroldadmin.lucilla.pipeline.transforms

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TokenizerTest : DescribeSpec({
    it("should tokenize input") {
        val input = "Valtteri, it's James"
        Tokenizer.process(input) shouldBe input.split(" ")
    }
})
