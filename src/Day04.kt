data class ScratchCard(val id: Int, val winningNumbers: List<Int>, val numbers: List<Int>) {
    fun countMatchingNumbers(): Int {
        return winningNumbers.count { it in numbers }
    }

    companion object {
        fun parseFrom(string: String): ScratchCard {
            return Regex("Card +(\\d+): +(.+) +\\| +(.+)").find(string)!!.destructured.let { (id, left, right) ->
                ScratchCard(
                    id.toInt(),
                    left.split(Regex(" +")).map { it.toInt() },
                    right.split(Regex(" +")).map { it.toInt() }
                )
            }
        }
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        return input
            .map { line ->
                ScratchCard.parseFrom(line)
            }.map { (_, winningNumbers, numbers) ->
                winningNumbers.filter { it in numbers }
            }.sumOf { numbers ->
                numbers.fold<Int, Int>(0) { acc, _ ->
                    if (acc == 0) 1
                    else acc * 2
                }
            }
    }

    fun part2(input: List<String>): Int {
        val cardsWithId = input.associate { line -> ScratchCard.parseFrom(line).let { it.id to it } }

        var scratchCardCount = 0
        val cardsWithCount = cardsWithId.values.associateWith { 1 }.toMutableMap()

        while (cardsWithCount.any { it.value != 0 }) {
            val (card, count) = cardsWithCount.entries.first { it.value != 0 }
            cardsWithCount[card] = 0

            for (i in 1..card.countMatchingNumbers()) {
                cardsWithCount.merge(cardsWithId[card.id + i]!!, count) { old, new -> old + new }
            }

            scratchCardCount += count
        }

        return scratchCardCount
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 13)

    check(part2(testInput) == 30)

    // apply on real input
    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}
