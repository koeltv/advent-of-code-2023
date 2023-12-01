private val stringDigits = mapOf(
    "one" to 1,
    "two" to 2,
    "three" to 3,
    "four" to 4,
    "five" to 5,
    "six" to 6,
    "seven" to 7,
    "eight" to 8,
    "nine" to 9,
)

fun main() {
    fun part1(input: List<String>): Int {
        return input
            .map { line -> line.mapNotNull { it.digitToIntOrNull() } }
            .sumOf { it.first() * 10 + it.last() }
    }

    fun part2(input: List<String>): Int {
        return input.map { line ->
            var buffer = ""
            line.mapNotNull { char ->
                buffer += char

                char.digitToIntOrNull() ?: stringDigits.entries
                    .firstOrNull { (string, _) ->
                        val digitIndex = buffer.indexOf(string)
                        if (digitIndex != -1) {
                            buffer = buffer.substring(digitIndex + 1)
                            true
                        } else false
                    }?.value
            }
        }.sumOf { it.first() * 10 + it.last() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput1 = readInput("Day01_test1")
    check(part1(testInput1) == 142)

    val testInput2 = readInput("Day01_test2")
    check(part2(testInput2) == 281)

    // apply on real input
    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
