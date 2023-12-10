data class Variation(val values: List<Int>) {
    val stages = generateSequence(values) { it.zipWithNext { a, b -> b - a } }
        .takeWhile { stage -> stage.any { it != 0 } }
        .toList()
}

fun main() {
    fun parse(input: List<String>): List<Variation> {
        return input.map { line ->
            Variation(line.split(" ").map { it.toInt() })
        }
    }

    fun part1(input: List<String>): Long {
        val variationStagesRightMostValues = parse(input).map { variation ->
            variation.stages.map { it.last() }
        }

        return variationStagesRightMostValues.sumOf { stages ->
            stages.reversed().fold(0L) { nextVariation, value ->
                value + nextVariation
            }
        }
    }

    fun part2(input: List<String>): Long {
        val variationStagesLeftMostValues = parse(input).map { variation ->
            variation.stages.map { it.first() }
        }

        return variationStagesLeftMostValues.sumOf { stages ->
            stages.reversed().fold(0L) { nextVariation, value ->
                value - nextVariation
            }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day09_test")
    check(part1(testInput) == 114L)

    check(part2(testInput) == 2L)

    // apply on real input
    val input = readInput("Day09")
    part1(input).println()
    part2(input).println()
}