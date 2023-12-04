import kotlin.math.max

fun main() {
    fun requiredCubeCount(input: List<String>): Map<Int, Map<String, Int>> {
        return input
            .filter { it.isNotBlank() }
            .associate { line ->
                Regex("Game (\\d+): (.+)").find(line)!!.destructured.let { (gameId, sets) ->
                    val cubesCount = sets.split(';').map { set ->
                        set.split(',').associate { cubeData ->
                            Regex("(\\d+) (\\w+)").find(cubeData)!!.destructured.let { (count, color) ->
                                color to count.toInt()
                            }
                        }
                    }.reduce { map1, map2 ->
                        map1.merge(map2) { _, count1, count2 -> max(count1, count2) }
                    }
                    gameId.toInt() to cubesCount
                }
            }
    }

    fun part1(input: List<String>, givenCubes: Map<String, Int>): Int {
        return requiredCubeCount(input).filterValues {
            it.all { (color, count) -> count <= givenCubes[color]!! }
        }.map { it.key }.sum()
    }

    fun part2(input: List<String>): Int {
        return requiredCubeCount(input).map { (_, cubeCount) ->
            cubeCount.values.reduce { a, b -> a * b }
        }.sum()
    }

    val givenCubes = mapOf(
        "red" to 12,
        "green" to 13,
        "blue" to 14,
    )

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput, givenCubes) == 8)

    check(part2(testInput) == 2286)

    // apply on real input
    val input = readInput("Day02")
    part1(input, givenCubes).println()
    part2(input).println()
}
