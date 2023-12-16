data class Beam(val coordinates: Coordinates, val direction: Direction) {
    private fun moveKnowingTile(tile: Char): List<Beam> {
        return if (tile == '.') {
            listOf(copy(coordinates = coordinates.moveToward(direction)))
        } else if (tile == '/' || tile == '\\') {
            val newDirection = when (direction) {
                Direction.NORTH -> Direction.EAST
                Direction.SOUTH -> Direction.WEST
                Direction.WEST -> Direction.SOUTH
                Direction.EAST -> Direction.NORTH
            }.let { if (tile == '\\') it.opposite() else it }
            listOf(copy(coordinates = coordinates.moveToward(newDirection), direction = newDirection))
        } else if (tile == '|') {
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                listOf(copy(coordinates = coordinates.moveToward(direction)))
            } else {
                listOf(
                    copy(coordinates = coordinates.moveToward(Direction.NORTH), direction = Direction.NORTH),
                    copy(coordinates = coordinates.moveToward(Direction.SOUTH), direction = Direction.SOUTH),
                )
            }
        } else if (tile == '-') {
            if (direction == Direction.WEST || direction == Direction.EAST) {
                listOf(copy(coordinates = coordinates.moveToward(direction)))
            } else {
                listOf(
                    copy(coordinates = coordinates.moveToward(Direction.WEST), direction = Direction.WEST),
                    copy(coordinates = coordinates.moveToward(Direction.EAST), direction = Direction.EAST),
                )
            }
        } else {
            error("Unknown encounter: $tile")
        }
    }

    fun propagateIn(map: List<String>, draw: List<String>.(List<Beam>) -> Unit = {}): List<Beam> {
        val existingBeams = mutableListOf(this)
        val onGoingBeams = mutableListOf(this)

        while (onGoingBeams.isNotEmpty()) {
            val beam = onGoingBeams.removeFirst()

            map.draw(existingBeams)

            val newBeams = beam.moveKnowingTile(map[beam.coordinates])
            newBeams
                .filter { it !in existingBeams && it.coordinates in map }
                .forEach {
                    existingBeams.add(it)
                    onGoingBeams.add(it)
                }
        }

        return existingBeams
    }
}

private fun List<String>.drawWithBeams(existingBeams: List<Beam>) {
    val beamDirections = existingBeams
        .groupBy({ it.coordinates }) { it.direction }
        .mapValues { (_, directions) ->
            directions.any { it == Direction.NORTH || it == Direction.SOUTH } to
                    directions.any { it == Direction.WEST || it == Direction.EAST }
        }

    val lines = indices.joinToString("\n", postfix = "\n") { y ->
        this[y].mapIndexed { x, char ->
            beamDirections[Coordinates(x, y)]?.let { (isAnyVertical, isAnyHorizontal) ->
                if (char == '.') {
                    val beamChar =
                        if (isAnyVertical && isAnyHorizontal) '┼'
                        else if (isAnyVertical) '│'
                        else '─'
                    beamChar.withColor(EscapeSequence.Color.YellowBright)
                } else {
                    char.withColor(EscapeSequence.Color.Red)
                }
            } ?: if (char == '.') " " else "$char"
        }.joinToString("")
    }
    clearTerminal()
    println(lines)
    Thread.sleep(10)
}

fun main() {
    fun part1(input: List<String>, withProgress: Boolean = false): Int {
        val startingBeam = Beam(Coordinates(0, 0), Direction.EAST)

        return startingBeam.propagateIn(input) { if (withProgress) drawWithBeams(it) }
            .also { input.drawWithBeams(it) }
            .distinctBy { it.coordinates }
            .count()
    }

    fun part2(input: List<String>, withProgress: Boolean = false): Int {
        val startingHorizontally = input.indices.flatMap {
            listOf(
                Beam(Coordinates(0, it), Direction.EAST),
                Beam(Coordinates(input[0].lastIndex, it), Direction.WEST),
            )
        }
        val startingVertically = input[0].indices.flatMap {
            listOf(
                Beam(Coordinates(it, 0), Direction.SOUTH),
                Beam(Coordinates(it, input.lastIndex), Direction.NORTH)
            )
        }

        val startingBeams = startingHorizontally + startingVertically

        return startingBeams
            .map { it.propagateIn(input) { beams -> if (withProgress) input.drawWithBeams(beams) } }
            .maxBy { it.distinctBy { beam -> beam.coordinates }.count() }
            .also { input.drawWithBeams(it) }
            .distinctBy { it.coordinates }
            .count()
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
    checkEqual(part1(testInput, withProgress = true), 46)
    checkEqual(part2(testInput, withProgress = true), 51)

// apply on real input
    val input = readInput("Day16")
    boldPrint("\nPart 1: ${part1(input)}\n")
    boldPrint("\nPart 2: ${part2(input)}\n")
}
