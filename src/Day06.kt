import kotlin.streams.toList

private const val BOAT_SPEED_INCREASE_RATE = 1

fun main() {
    fun countPossibilities(races: List<Pair<Long, Long>>): List<Int> {
        return races
            .map { (maxTime, minDistance) ->
                (1 until maxTime).toStream().parallel().map { pressTime ->
                    (maxTime - pressTime) * (BOAT_SPEED_INCREASE_RATE * pressTime)
                }.filter { it > minDistance }.toList()
            }
            .map { it.count() }
    }

    fun part1(input: List<String>): Int {
        val times = input[0].substringAfter(":").trim().split(Regex(" +")).map { it.toLong() }
        val distances = input[1].substringAfter(":").trim().split(Regex(" +")).map { it.toLong() }

        val races = times.mapIndexed { i, time -> time to distances[i] }

        return countPossibilities(races).reduce { product, value -> product * value }
    }

    fun part2(input: List<String>): Int {
        val time = input[0].substringAfter(":").trim().replace(" ", "").toLong()
        val distance = input[1].substringAfter(":").trim().replace(" ", "").toLong()

        val race = time to distance

        return countPossibilities(listOf(race)).first()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput) == 288)

    check(part2(testInput) == 71503)

    // apply on real input
    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}
