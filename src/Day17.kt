import EscapeSequence.Color.Companion.ColorPalette
import java.util.PriorityQueue
import kotlin.math.min

private data class Crucible(
    val coordinates: Coordinates,
    val direction: Direction,
    val sameDirectionCount: Int,
    val score: Int,
    val parent: Crucible? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Crucible

        if (coordinates != other.coordinates) return false
        if (direction != other.direction) return false
        if (sameDirectionCount != other.sameDirectionCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coordinates.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + sameDirectionCount
        return result
    }
}

private class CityMap(input: List<String>) {
    val map = input.map { line -> line.map { it.digitToInt() } }
    val endPoint = Coordinates(map[0].lastIndex, map.lastIndex)

    private val possiblePaths = mutableListOf<List<Coordinates>>()

    fun shortestPaths(minLength: Int, maxLength: Int): Int {
        val lowestLossPerPoint = mutableMapOf<Crucible, Int>()
        val queue = PriorityQueue<Crucible>(compareBy { it.score })

        listOf(Direction.SOUTH, Direction.EAST).forEach { direction ->
            val start = Crucible(Coordinates(0, 0), direction, 0, 0)
            queue.add(start)
            lowestLossPerPoint[start] = 0
        }

        var lowestHeatLoss = Int.MAX_VALUE

        while (queue.isNotEmpty()) {
            val point = queue.poll()
            if (point.score > lowestHeatLoss) break

            if (point.sameDirectionCount < maxLength) {
                val newCoordinates = point.coordinates.moveToward(point.direction)
                if (newCoordinates in map) {
                    val newScore = point.score + map[newCoordinates]
                    if (newCoordinates == endPoint) {
                        lowestHeatLoss = min(lowestHeatLoss, newScore)
                    }

                    fun tryAdd(newPoint: Crucible) {
                        if (newPoint.score > lowestHeatLoss) return

                        val current = lowestLossPerPoint[newPoint]
                        if (current == null || current > newScore) {
                            lowestLossPerPoint[newPoint] = newScore
                            queue.add(newPoint)
                        }
                    }

                    tryAdd(Crucible(newCoordinates, point.direction, point.sameDirectionCount + 1, newScore, point))

                    if (point.sameDirectionCount + 1 >= minLength) {
                        Direction.entries
                            .filter { point.direction.isHorizontal() == !it.isHorizontal() }
                            .forEach { direction ->
                                tryAdd(Crucible(newCoordinates, direction, 0, newScore, point))
                            }
                    }
                }
            }
        }

        possiblePaths.clear()
        lowestLossPerPoint
            .filter { (point, value) -> point.coordinates == endPoint && value == lowestHeatLoss }
            .keys
            .fold(emptySet<Crucible>()) { acc, crucible ->
                if (acc.none { it.parent == crucible.parent }) acc + crucible
                else acc
            }.map { reconstructPath(it) }
            .map { path -> path.map { it.coordinates } }
            .forEach { possiblePaths.add(it) }

        return lowestHeatLoss
    }

    private fun reconstructPath(node: Crucible): List<Crucible> {
        val path = mutableListOf<Crucible>()
        var current: Crucible? = node
        while (current != null) {
            path.add(current)
            current = current.parent
        }
        return path.reversed()
    }

    fun drawPaths() {
        val lines = map.mapIndexed { y, line ->
            line.mapIndexed { x, heatLoss ->
                val coordinates = Coordinates(x, y)
                val colorIndex = possiblePaths.indexOfFirst { coordinates in it }
                if (colorIndex >= 0) "$heatLoss".withColor(ColorPalette[colorIndex % ColorPalette.size])
                else "$heatLoss"
            }.joinToString("")
        }.joinToString("\n", postfix = "\n")
        println(lines)
    }
}


fun main() {
    fun part1(input: List<String>): Int {
        val map = CityMap(input)
        return map.shortestPaths(1, 3).also { map.drawPaths() }
    }

    fun part2(input: List<String>): Int {
        val map = CityMap(input)
        return map.shortestPaths(4, 10).also { map.drawPaths() }
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test")
    checkEqual(part1(testInput), 102)
    checkEqual(part2(testInput), 94)

// apply on real input
    val input = readInput("Day17")
    boldPrint("\nPart 1: ${part1(input)}\n")
    boldPrint("\nPart 2: ${part2(input)}\n")
}