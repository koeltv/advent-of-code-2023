private fun String.hash(): Int = fold(0) { acc, char ->
    ((acc + char.code) * 17) % 256
}

data class Lens(val label: String, val focalLength: Int)

fun main() {
    fun part1(input: List<String>): Int {
        check(input.size == 1)

        return input[0].split(',').sumOf { it.hash() }
    }

    fun part2(input: List<String>): Int {
        check(input.size == 1)

        val hashMap = buildMap<Int, MutableList<Lens>>(256) {
            putAll((0..<256).associateWith { mutableListOf() })
        }

        input[0].split(',').map { step ->
            Regex("([a-zA-Z]+)([=\\-])(\\d+)?").find(step)?.let { match ->
                val (label, operation) = match.destructured
                val focalLength =
                    if (operation == "=") match.destructured.component3().toInt()
                    else null

                hashMap.getValue(label.hash()).apply {
                    if (operation == "-") removeIf { it.label == label }
                    else {
                        check(focalLength != null)

                        val oldLensIndex = indexOfFirst { it.label == label }
                        val newLens = Lens(label, focalLength)
                        if (oldLensIndex != -1) this[oldLensIndex] = newLens
                        else add(newLens)
                    }
                }
            } ?: throw IllegalArgumentException("$step is not a valid step")
        }

        return hashMap.map { (boxNumber, lenses) ->
            lenses.mapIndexed { i, lens -> (1 + boxNumber) * (i + 1) * lens.focalLength }.sum()
        }.sum()
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    checkEqual(part1(testInput), 1320)
    checkEqual(part2(testInput), 145)

// apply on real input
    val input = readInput("Day15")
    boldPrint("\nPart 1: ${part1(input)}\n")
    boldPrint("\nPart 2: ${part2(input)}\n")
}
