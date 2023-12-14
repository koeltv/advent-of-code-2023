abstract class DataPoint(open val coordinates: Coordinates) {
    abstract fun near(coordinates: Coordinates): Boolean
}

data class PartNumber(override val coordinates: Coordinates, val number: Int) : DataPoint(coordinates) {
    override fun near(coordinates: Coordinates): Boolean {
        val digitCount = "$number".length
        val (x, y) = this.coordinates
        return (0 until digitCount).any { dx -> Coordinates(x + dx, y).near(coordinates) }
    }
}

data class Symbol(override val coordinates: Coordinates, val symbol: Char) : DataPoint(coordinates) {
    override fun near(coordinates: Coordinates): Boolean {
        return coordinates.near(coordinates)
    }
}

data class Schematic(val partNumbers: List<PartNumber>, val symbols: List<Symbol>) {
    companion object {
        fun from(dataPoints: List<String>): Schematic {
            // Find numbers and symbols in 2D String
            val (numbers, symbols) = dataPoints.flatMapIndexed { y, line ->
                line.mapIndexedNotNull { x, char ->
                    if (char != '.') {
                        Symbol(Coordinates(x, y), char)
                    } else null
                }
            }.partition { it.symbol.isDigit() }

            // Merge and parse part numbers
            val partNumbers = numbers
                .fold(mutableListOf<PartNumber>()) { partNumbers, dataPoint ->
                    partNumbers.find {
                        it.near(dataPoint.coordinates)
                    }?.let { neighbourPartNumber ->
                        partNumbers.remove(neighbourPartNumber)
                        val (coordinates, number) = neighbourPartNumber
                        if (coordinates.x < dataPoint.coordinates.x) {
                            val newNumber = number * 10 + dataPoint.symbol.digitToInt()
                            coordinates to newNumber
                        } else {
                            val newNumber = "${dataPoint.symbol}$number".toInt()
                            dataPoint.coordinates to newNumber
                        }
                    }?.let { (coordinates, number) ->
                        partNumbers.add(PartNumber(coordinates, number))
                    } ?: partNumbers.add(PartNumber(dataPoint.coordinates, dataPoint.symbol.digitToInt()))
                    partNumbers
                }.filter { partNumber -> symbols.any { partNumber.near(it.coordinates) } }

            return Schematic(partNumbers, symbols)
        }
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        return Schematic.from(input).partNumbers.sumOf { it.number }
    }

    fun part2(input: List<String>): Int {
        return Schematic.from(input).run {
            symbols
                .filter { it.symbol == '*' }
                .mapNotNull {
                    val nearbyPartNumbers = partNumbers.filter { partNumber -> partNumber.near(it.coordinates) }
                    if (nearbyPartNumbers.count() == 2) {
                        nearbyPartNumbers[0].number * nearbyPartNumbers[1].number
                    } else null
                }.sum()
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 4361)

    check(part2(testInput) == 467835)

    // apply on real input
    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}
