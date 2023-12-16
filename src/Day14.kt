fun List<List<Char>>.moveAllToward(direction: Direction): List<List<Char>> {
    val mutableMap = map { it.toMutableList() }.toMutableList()

    for (y in if (direction == Direction.SOUTH) indices.reversed() else indices) {
        for (x in if (direction == Direction.EAST) mutableMap[y].indices.reversed() else mutableMap[y].indices) {
            if (mutableMap[y][x] != 'O') continue

            val increment = when (direction) {
                Direction.NORTH -> Coordinates(0, -1)
                Direction.SOUTH -> Coordinates(0, 1)
                Direction.WEST -> Coordinates(-1, 0)
                Direction.EAST -> Coordinates(1, 0)
            }

            var current = Coordinates(x, y)
            var next = current + increment

            while (next in mutableMap && mutableMap[next] == '.') {
                mutableMap[current] = '.'
                mutableMap[next] = 'O'
                current += increment
                next += increment
            }
        }
    }

    return mutableMap
}

fun List<List<Char>>.calculateNorthSupportLoad(): Int {
    return this
        .mapIndexed { y, line -> line.count { it == 'O' } * (size - y) }
        .sum()
}

/**
 * Find the smallest repeating cycle.
 * To be taken into account, the cycle should be touching each other
 *
 * For example:
 * ```
 * 87, 69, 69, 69, 65, 64, 65, 63, 68, 69, 69, 65, 64, 65, 63, 68, 69, 69, 65, 64
 *         ^                           ^                           ^
 * ```
 *
 * @param nums
 * @return
 */
fun findCycle(nums: List<Int>): IntRange? {
    val maxLength = nums.size / 2 // Maximum possible length of the repeating pattern

    for (windowSize in 2..maxLength) {
        for (offset in 0..nums.size - windowSize * 2) {
            val window = nums.subList(offset, offset + windowSize)
            val nextWindow = nums.subList(offset + windowSize, offset + windowSize * 2)

            if (window == nextWindow) {
                return offset..<offset + windowSize
            }
        }
    }

    return null
}

fun main() {
    fun part1(input: List<String>): Int {
        val newMap = input
            .map { it.toList() }
            .moveAllToward(Direction.NORTH)

        return newMap.calculateNorthSupportLoad()
    }

    fun part2(input: List<String>, totalCycle: Int = 1_000_000_000): Int {
        var map = input.map { it.toList() }
        val northSupportLoads = mutableListOf(map.calculateNorthSupportLoad())

        do {
            map = map
                .moveAllToward(Direction.NORTH)
                .moveAllToward(Direction.WEST)
                .moveAllToward(Direction.SOUTH)
                .moveAllToward(Direction.EAST)
            northSupportLoads.add(map.calculateNorthSupportLoad())
        } while (findCycle(northSupportLoads) == null)

        val patternRange = findCycle(northSupportLoads)!!
        val pattern = northSupportLoads.subList(patternRange.first, patternRange.last + 1)

        // If we want a value before the pattern appeared
        return if (totalCycle < patternRange.first) northSupportLoads[totalCycle]
        // If we want a value contained in the pattern
        else pattern[(totalCycle - patternRange.first) % pattern.size]
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
    checkEqual(part1(testInput), 136)
    checkEqual(part2(testInput), 64)

// apply on real input
    val input = readInput("Day14")
    boldPrint("\nPart 1: ${part1(input)}\n")
    boldPrint("\nPart 2: ${part2(input)}\n")
}
