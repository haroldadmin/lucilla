package com.haroldadmin.lucilla.core.rank

/**
 * Calculates the Edit Distance (Levenshtein Distance) between
 * the given Strings.
 *
 * @param a The first string
 * @param b The second string
 * @param deletionCost The cost of a deletion operation
 * @param insertionCost The cost of an insertion operation
 * @param replacementCost The cost of a replacement operation
 */
public fun ld(
    a: String,
    b: String,
    deletionCost: Int = 1,
    insertionCost: Int = 1,
    replacementCost: Int = 1,
): Int {
    if (a.isEmpty()) {
        return b.length
    }

    if (b.isEmpty()) {
        return a.length
    }

    val dpTable: Array<IntArray> = Array(a.length + 1) {
        IntArray(b.length + 1)
    }

    for (i in 1..a.length) {
        dpTable[i][0] = i
    }
    for (i in 1..b.length) {
        dpTable[0][i] = i
    }

    for (i in 1..a.length) {
        val aChar = a[i - 1]
        for (j in 1..b.length) {
            val bChar = b[j - 1]
            if (aChar == bChar) {
                val editCost = dpTable[i - 1][j - 1]
                dpTable[i][j] = editCost
                continue
            }

            val costWithReplacement = dpTable[i - 1][j - 1] + replacementCost
            val costWithDeletion = dpTable[i][j - 1] + deletionCost
            val costWithInsertion = dpTable[i - 1][j] + insertionCost
            dpTable[i][j] = minOf(costWithReplacement, costWithDeletion, costWithInsertion)
        }
    }

    return dpTable[a.length][b.length]
}
