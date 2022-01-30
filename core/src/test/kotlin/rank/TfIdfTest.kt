package com.haroldadmin.lucilla.core.rank

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec

class TfIdfTest : DescribeSpec({
    describe("Inverse Document Frequency") {
        it("should not return an error if document frequency is 0") {
            shouldNotThrowAny { tfIdf(1.0, 0, 1) }
        }
    }
})
