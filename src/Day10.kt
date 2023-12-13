operator fun Collection<CharSequence>.contains(point: Coordinates): Boolean {
    return point.y in indices && point.x in first().indices
}

data class PipeMaze(val map: List<String>) {
    val loopTileWithDistance: Map<Coordinates, Int> = findLoopTiles()

    private val loopTiles = loopTileWithDistance.keys.toList()

    val enclosedTiles: List<Coordinates> by lazy {
        val knownEnclosedTiles = mutableListOf<Coordinates>()
        val knownUnenclosedTiles = mutableListOf<Coordinates>()

        for (y in map.indices) {
            progressBar(y, map.lastIndex)
            for (x in map[y].indices) {
                val tile = Coordinates(x, y)

                if (
                    map[tile] == ' ' ||
                    tile in loopTiles ||
                    tile in knownEnclosedTiles ||
                    tile in knownUnenclosedTiles
                ) continue

                val (tileGroup, isEnclosed) = checkIfEnclosed(tile)
                if (isEnclosed) knownEnclosedTiles += tileGroup.filter { map[it.y][it.x] != ' ' }
                else knownUnenclosedTiles += tileGroup
            }
        }
        knownEnclosedTiles
    }

    private fun Coordinates.propagate(): List<Coordinates> {
        return if (this !in map) emptyList()
        else when (map[this]) {
            '|' -> listOf(north(), south())
            '─' -> listOf(west(), east())
            '└' -> listOf(north(), east())
            '┘' -> listOf(west(), north())
            '┐' -> listOf(west(), south())
            '┌' -> listOf(south(), east())
            '.', ' ' -> emptyList()
            '█' -> neighbors().filter { this in it.propagate() }
            else -> error("Unknown symbol: ${map[this]}")
        }
    }

    private fun findLoopTiles(): Map<Coordinates, Int> {
        val tilesToVisit = ArrayDeque<Pair<Coordinates, Int>>()
        val loopTiles = mutableMapOf<Coordinates, Int>()

        tilesToVisit.addLast(first('█') to 0)

        while (tilesToVisit.isNotEmpty()) {
            val (coordinates, steps) = tilesToVisit.removeFirst()

            tilesToVisit.addAll(
                coordinates.propagate()
                    .filter { it !in loopTiles.keys && it in map && map[it] != '.' }
                    .map { it to steps + 1 })

            loopTiles[coordinates] = steps
        }
        return loopTiles
    }

    fun expand(): PipeMaze {
        val newMap = MutableList(map.size * 2) { "" }

        map.indices.forEach { y ->
            map[y].indices.forEach { x ->
                val tile = Coordinates(x, y)
                val char = map[tile]

                val horizontalChar =
                    if (x >= map[y].lastIndex) ""
                    else if (tile in loopTiles && tile.connectsTo(Coordinates(x + 1, y))) "─"
                    else " "

                newMap[y * 2] += "$char$horizontalChar"
                if (y < map.lastIndex) {
                    val verticalChar =
                        if (tile in loopTiles && tile.connectsTo(Coordinates(x, y + 1))) "|"
                        else " "
                    newMap[y * 2 + 1] += "$verticalChar${if (x < map[y].lastIndex) " " else ""}"
                }
            }
        }

        return PipeMaze(newMap)
    }

    fun visualize() {
        map.indices.forEach { y ->
            map[y].indices.forEach { x ->
                print(map[y][x])
            }
            kotlin.io.println()
        }
    }

    fun visualizeWithSteps() {
        val padding = "${loopTileWithDistance.maxOf { it.value }}".length + 1
        val halfSeparation = (1 until padding * (map[0].length / 2)).map { '-' }.joinToString("")
        println("\n${halfSeparation}Map${halfSeparation}")
        map.indices.forEach { y ->
            map[y].indices.forEach { x ->
                val tile = (loopTileWithDistance[Coordinates(x, y)]?.toString() ?: ".").padEnd(padding)
                print(tile)
            }
            kotlin.io.println()
        }
        println("\n${halfSeparation}---${halfSeparation}")
    }

    fun visualizeEnclosed() {
        val halfSeparation = (1..(map[0].length / 2 - 6)).map { '-' }.joinToString("")
        println("\n${halfSeparation}Map-Enclosed${halfSeparation}")
        map.indices.forEach { y ->
            map[y].indices.forEach { x ->
                when (Coordinates(x, y)) {
                    in enclosedTiles -> colorPrint('I', EscapeSequence.Color.GreenBold)
                    in loopTiles -> colorPrint(map[y][x], EscapeSequence.Color.Red)
                    else -> print(' ')
                }
            }
            kotlin.io.println()
        }
        println("${halfSeparation}------------${halfSeparation}\n")
    }

    private fun checkIfEnclosed(
        coordinates: Coordinates,
        suspectedTiles: List<Coordinates> = emptyList()
    ): Pair<List<Coordinates>, Boolean> {
        val groupedTiles = suspectedTiles.toMutableList().also { it.add(coordinates) }

        var index = groupedTiles.lastIndex
        while (index < groupedTiles.size) {
            groupedTiles[index++]
                .neighbors()
                .filter { it !in loopTiles && it !in groupedTiles }
                .forEach {
                    if (it !in map) return (groupedTiles + it) to false
                    else groupedTiles.add(it)
                }
        }

        return groupedTiles to true
    }

    private fun Coordinates.connectsTo(tile: Coordinates): Boolean = tile in this.propagate()

    fun first(char: Char): Coordinates {
        return map.indices.firstNotNullOf { y ->
            map[y].indices.firstOrNull { x ->
                map[y][x] == char
            }?.let { x -> Coordinates(x, y) }
        }
    }

    private operator fun List<String>.get(tile: Coordinates): Char = map[tile.y][tile.x]

    companion object {
        fun fromUncleanedInput(input: List<String>): PipeMaze {
            return PipeMaze(input.map {
                it
                    .replace('-', '─')
                    .replace('F', '┌')
                    .replace('L', '└')
                    .replace('7', '┐')
                    .replace('J', '┘')
                    .replace('S', '█')
            })
        }
    }
}

fun Coordinates.north(): Coordinates = Coordinates(x, y - 1)
fun Coordinates.south(): Coordinates = Coordinates(x, y + 1)
fun Coordinates.west(): Coordinates = Coordinates(x - 1, y)
fun Coordinates.east(): Coordinates = Coordinates(x + 1, y)
fun Coordinates.neighbors(): List<Coordinates> = listOf(north(), south(), west(), east())

fun main() {
    fun part1(input: List<String>): Int {
        val maze = PipeMaze.fromUncleanedInput(input)

        maze.visualize()
        maze.visualizeWithSteps()

        return maze.loopTileWithDistance.maxOf { (_, steps) -> steps }
    }

    fun part2(input: List<String>): Int {
        val maze = PipeMaze.fromUncleanedInput(input)

        maze.visualize()

        val expendedMaze = maze.expand()

        val enclosedTiles = expendedMaze.enclosedTiles
        expendedMaze.visualizeEnclosed()

        return enclosedTiles.count()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 8)

    check(part2(readInput("Day10_test21")) == 4)
    check(part2(readInput("Day10_test22")) == 4)
    check(part2(readInput("Day10_test23")) == 8)
    check(part2(readInput("Day10_test24")) == 10)

    // apply on real input
    val input = readInput("Day10")
    println("\nPart 1: ${part1(input)}\n")
    println("\nPart 2: ${part2(input)}\n")
}
