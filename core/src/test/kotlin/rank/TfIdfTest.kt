package com.haroldadmin.lucilla.core.rank

import com.haroldadmin.lucilla.core.Sentence
import com.haroldadmin.lucilla.core.useFts
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TfIdfTest : DescribeSpec({
    describe("Term Frequency") {
        it("should return correct scaled term frequency for a document") {
            val index = useFts<Sentence>()
            val sentence = Sentence(line = 0, value = "No Michael no! This is so not right.")
            index.add(sentence)

            val sentenceTokens = index.pipeline.process(sentence.value)
            val tf = termFrequency(
                term = "michael",
                docId = sentence.line,
                docLength = sentenceTokens.size,
                index = index.index
            )
            tf shouldBe 1.0 / sentenceTokens.size
        }

        it("should return 0 if the term does not exist in the document") {
            val index = useFts<Sentence>()
            val sentence = Sentence(line = 0, value = "No Michael no! This is so not right.")
            index.add(sentence)

            val sentenceTokens = index.pipeline.process(sentence.value)
            val tf = termFrequency(
                term = "toto",
                docId = sentence.line,
                docLength = sentenceTokens.size,
                index = index.index
            )
            tf shouldBe 0.0
        }

        it("should return 0 if the document does not exist") {
            val index = useFts<Sentence>()
            val sentence = Sentence(line = 0, value = "No Michael no! This is so not right.")
            index.add(sentence)

            val sentenceTokens = index.pipeline.process(sentence.value)
            val tf = termFrequency(
                term = "michael",
                docId = sentence.line + 1,
                docLength = sentenceTokens.size,
                index = index.index
            )
            tf shouldBe 0.0
        }
    }

    describe("Document Frequency") {
        it("should return correct DF for a token in a single document") {
            val index = useFts<Sentence>().apply {
                add(Sentence(line = 0, "Michael I have sent you an email"))
                add(Sentence(line = 1, "Michael have you received my email?"))
            }

            val freq = documentFrequency("michael", index.index)
            freq shouldBe 2
        }

        it("should return 0 if the term does not exist") {
            val index = useFts<Sentence>().apply {
                add(Sentence(line = 0, "Michael I have sent you an email"))
                add(Sentence(line = 1, "Michael have you received my email?"))
            }

            val freq = documentFrequency("toto", index.index)
            freq shouldBe 0
        }
    }

    describe("Inverse Document Frequency") {
        it("should not return an error if document frequency is 0") {
            shouldNotThrowAny { tfIdf(1.0, 0, 1) }
        }
    }
})