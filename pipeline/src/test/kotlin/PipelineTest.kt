package com.haroldadmin.lucilla.pipeline

import com.haroldadmin.lucilla.pipeline.transforms.LowercaseTransform
import com.haroldadmin.lucilla.pipeline.transforms.StripPunctuationTransform
import com.haroldadmin.lucilla.pipeline.transforms.Tokenizer
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class PipelineTest : DescribeSpec({
    it("should return input as is if the pipeline is empty") {
        val input = "El plan is working"
        val pipeline = Pipeline.of()
        val (output) = pipeline.process(input)
        output shouldBe input
    }

    it("should run the input through every step of the pipeline") {
        val input = "El plan is working!"
        val output = Pipeline
            .of(LowercaseTransform, StripPunctuationTransform, Tokenizer)
            .process(input)
        output shouldContainAll listOf("el", "plan", "is", "working")
    }

    it("should throw an error if a pipeline step throws an error") {
        val input = "El plan is working"
        shouldThrowAny {
            Pipeline.of({ throw Error("test") }).process(input)
        }
    }

    it("should pass the output of the first step as input of the next step") {
        val result = Pipeline.of(
            { input -> input + input },
            { input -> input + input },
        ).process("first")

        result shouldHaveSize 4
    }
})
