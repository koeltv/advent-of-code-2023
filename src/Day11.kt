import kotlin.math.abs

data class LCoordinates(val x: Long, val y: Long)

data class Picture(val grid: List<String>, val expansionFactor: Long = 2) {
    private val emptyRowsIndexes: List<Int> = grid.withIndex()
        .filter { (_, row) -> row.all { it == '.' } }
        .map { it.index }

    private val emptyColumnsIndexes: List<Int> = grid[0].indices
        .filter { x -> grid.all { it[x] == '.' } }

    private val galaxies = grid.flatMapIndexed { y, line ->
        line.withIndex()
            .filter { (_, c) -> c == '#' }
            .map { (x, _) -> adaptToExpansion(x, y) }
    }

    private fun adaptToExpansion(x: Int, y: Int): LCoordinates {
        val dy = emptyRowsIndexes.count { it < y } * (expansionFactor - 1)
        val dx = emptyColumnsIndexes.count { it < x } * (expansionFactor - 1)
        return LCoordinates(x + dx, y + dy)
    }

    private fun findAllPairs(): List<Pair<LCoordinates, LCoordinates>> {
        return galaxies.indices.flatMap { i ->
            (i + 1..galaxies.lastIndex).map { j ->
                galaxies[i] to galaxies[j]
            }
        }
    }

    fun distanceBetweenGalaxies(): Map<Pair<LCoordinates, LCoordinates>, Long> {
        return findAllPairs().associateWith { (first, second) -> first.distanceFrom(second) }
    }

    fun show() {
        grid.forEachIndexed { y, line ->
            line.forEachIndexed { x, c ->
                if (adaptToExpansion(x, y) in galaxies) colorPrint("#", EscapeSequence.Color.Green)
                else if (x in emptyColumnsIndexes && y in emptyRowsIndexes) print("┼")
                else if (x in emptyColumnsIndexes) print("│")
                else if (y in emptyRowsIndexes) print("─")
                else print("$c")
            }
            kotlin.io.println()
        }
    }
}

fun LCoordinates.distanceFrom(point: LCoordinates): Long = abs(x - point.x) + abs(y - point.y)

fun main() {
    fun part1(input: List<String>): Long {
        val picture = Picture(input)
        return picture.distanceBetweenGalaxies().values.sum()
    }

    fun part2(input: List<String>, delta: Long = 1_000_000): Long {
        val picture = Picture(input, delta)
        picture.show()
        return picture.distanceBetweenGalaxies().values.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day11_test")
    checkEqual(part1(testInput), 374L)
    checkEqual(part2(testInput, 10), 1030L)
    checkEqual(part2(testInput, 100), 8410L)

    // apply on real input
    val input = readInput("Day11")
    println("\nPart 1: ${part1(input)}\n")
    println("\nPart 2: ${part2(input)}\n")
}
