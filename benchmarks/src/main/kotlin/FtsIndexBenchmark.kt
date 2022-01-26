package com.haroldadmin.lucilla.benchmarks

import com.haroldadmin.lucilla.annotations.Id
import com.haroldadmin.lucilla.core.useFts
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Setup

data class TestData(@Id val id: Int, val value: String)

@State(Scope.Benchmark)
class FtsIndexBenchmark {
    @Param("100", "1000", "10000")
    var docCount: Int = 0

    var data = listOf<TestData>()

    @Setup
    fun setup() {
        data = generateSequence(0) { it + 1 }
            .map { id -> TestData(id, "Document $id") }
            .take(docCount).toList()
    }

    @Benchmark
    fun addDocumentsToIndexBenchmark(blackHole: Blackhole) {
        blackHole.consume(useFts(data))
    }
}