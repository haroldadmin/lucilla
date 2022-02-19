package com.haroldadmin.lucilla.benchmarks

import com.haroldadmin.lucilla.core.rank.ld
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole

@State(Scope.Benchmark)
class LevenshteinDistanceBenchmark {
    @Benchmark
    fun ldBenchmark(blackHole: Blackhole) {
        blackHole.consume(ld("levenshtein", "edit"))
    }
}
