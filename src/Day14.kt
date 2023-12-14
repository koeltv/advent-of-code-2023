// TODO Compact all 4 move methods into 1

fun List<List<Char>>.moveAllNorth(): List<List<Char>> {
    val mutableMap = map { it.toMutableList() }.toMutableList()

    for (y in mutableMap.indices) {
        for (x in mutableMap[y].indices) {
            if (y == 0 || mutableMap[y][x] != 'O') continue

            var dy = y - 1
            while (dy >= 0 && mutableMap[dy][x] == '.') {
                mutableMap[dy + 1][x] = '.'
                mutableMap[dy--][x] = 'O'
            }
        }
    }

    return mutableMap
}

fun List<List<Char>>.moveAllSouth(): List<List<Char>> {
    val mutableMap = map { it.toMutableList() }.toMutableList()

    for (y in mutableMap.indices.reversed()) {
        for (x in mutableMap[y].indices) {
            if (y == mutableMap.lastIndex || mutableMap[y][x] != 'O') continue

            var dy = y + 1
            while (dy <= mutableMap.lastIndex && mutableMap[dy][x] == '.') {
                mutableMap[dy - 1][x] = '.'
                mutableMap[dy++][x] = 'O'
            }
        }
    }

    return mutableMap
}

fun List<List<Char>>.moveAllWest(): List<List<Char>> {
    val mutableMap = map { it.toMutableList() }.toMutableList()

    for (x in mutableMap[0].indices) {
        for (y in mutableMap.indices) {
            if (x == 0 || mutableMap[y][x] != 'O') continue

            var dx = x - 1
            while (dx >= 0 && mutableMap[y][dx] == '.') {
                mutableMap[y][dx + 1] = '.'
                mutableMap[y][dx--] = 'O'
            }
        }
    }

    return mutableMap
}

fun List<List<Char>>.moveAllEast(): List<List<Char>> {
    val mutableMap = map { it.toMutableList() }.toMutableList()

    for (x in mutableMap[0].indices.reversed()) {
        for (y in mutableMap.indices) {
            if (x == mutableMap[0].lastIndex || mutableMap[y][x] != 'O') continue

            var dx = x + 1
            while (dx <= mutableMap[0].lastIndex && mutableMap[y][dx] == '.') {
                mutableMap[y][dx - 1] = '.'
                mutableMap[y][dx++] = 'O'
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
            .moveAllNorth()

        return newMap.calculateNorthSupportLoad()
    }

    fun part2(input: List<String>, totalCycle: Int = 1_000_000_000): Int {
        var map = input.map { it.toList() }
        val northSupportLoads = mutableListOf(map.calculateNorthSupportLoad())

        do {
            map = map
                .moveAllNorth()
                .moveAllWest()
                .moveAllSouth()
                .moveAllEast()
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
