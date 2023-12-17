data class Beam(val coordinates: Coordinates, val direction: Direction) {
    fun moveKnowingTile(tile: Char): List<Beam> {
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
}

data class BeamMap(val map: List<String>, val startingBeam: Beam) {
    private val beamsWithDirections = mutableMapOf<Coordinates, Pair<Boolean, Boolean>>()

    private fun draw() {
        val lines = map.indices.joinToString("\n", postfix = "\n") { y ->
            map[y].mapIndexed { x, char ->
                beamsWithDirections[Coordinates(x, y)]?.let { (isAnyVertical, isAnyHorizontal) ->
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
    }

    fun showPropagatedBeam() {
        if (beamsWithDirections.isNotEmpty()) {
            draw()
        } else {
            colorPrintln("Beam was not propagated yet !", EscapeSequence.Color.RedBold)
        }
    }

    fun propagate(draw: Boolean = false): List<Coordinates> {
        if (beamsWithDirections.isNotEmpty()) {
            if (draw) draw()
            return beamsWithDirections.keys.toList()
        }

        val existingBeams = mutableListOf(startingBeam)
        val onGoingBeams = mutableListOf(startingBeam)

        var bufferIndex = onGoingBeams.size
        while (onGoingBeams.isNotEmpty()) {
            val beam = onGoingBeams.removeFirst()

            beamsWithDirections.compute(beam.coordinates) { _, previous ->
                val isVertical = beam.direction == Direction.NORTH || beam.direction == Direction.SOUTH
                (isVertical || previous?.first == true) to (!isVertical || previous?.second == true)
            }

            val newBeams = beam.moveKnowingTile(map[beam.coordinates])
            newBeams
                .filter { it !in existingBeams && it.coordinates in map }
                .forEach {
                    existingBeams.add(it)
                    onGoingBeams.add(it)
                }

            if (draw && --bufferIndex <= 0) {
                draw()
                bufferIndex = onGoingBeams.size
            }
        }

        return existingBeams.map { it.coordinates }.distinct()
    }
}

fun main() {
    fun part1(input: List<String>, withProgress: Boolean = false): Int {
        val startingBeam = Beam(Coordinates(0, 0), Direction.EAST)
        val map = BeamMap(input, startingBeam)

        return map.propagate(withProgress).also { map.showPropagatedBeam() }.count()
    }

    fun part2(input: List<String>, withProgress: Boolean = false): Int {
        val startingBeams = input.indices
            .flatMap {
                listOf(
                    Beam(Coordinates(0, it), Direction.EAST),
                    Beam(Coordinates(input[0].lastIndex, it), Direction.WEST),
                )
            } + input[0].indices.flatMap {
            listOf(
                Beam(Coordinates(it, 0), Direction.SOUTH),
                Beam(Coordinates(it, input.lastIndex), Direction.NORTH)
            )
        }
        return startingBeams
            .map { BeamMap(input, it) }
            .maxBy { it.propagate(withProgress).count() }
            .also { it.showPropagatedBeam() }
            .propagate().count()
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
    checkEqual(part1(testInput, withProgress = true), 46)
    checkEqual(part2(testInput, withProgress = true), 51)

// partial real input for visualisation purposes
    val visualInput = readInput("Day16_visual")
    boldPrint("\nVisualization: ${part1(visualInput, withProgress = true)}\n")

// apply on real input
    val input = readInput("Day16")
    boldPrint("\nPart 1: ${part1(input)}\n")
    boldPrint("\nPart 2: ${part2(input)}\n")
}
