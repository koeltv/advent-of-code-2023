import java.util.concurrent.atomic.AtomicInteger

private val knownSolutions = mutableMapOf<SpringGroup, List<String>>()

data class SpringGroup(
    val arrangement: String,
    val groups: IntArray,
    val previousSeparator: String = "",
    val nextSeparator: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpringGroup

        if (arrangement != other.arrangement) return false
        if (!groups.contentEquals(other.groups)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = arrangement.hashCode()
        result = 31 * result + groups.contentHashCode()
        return result
    }

    operator fun plus(springGroup: SpringGroup): SpringGroup {
        check(nextSeparator == springGroup.previousSeparator)
        return SpringGroup(
            arrangement + nextSeparator + springGroup.arrangement,
            groups + springGroup.groups,
            previousSeparator,
            springGroup.nextSeparator
        )
    }

    private fun goThroughCombinations(): List<String> {
        // Max amount of ? to replace
        val maxReplacement = groups.sum()

//        println(arrangement)
        var possibleArrangements = listOf(arrangement)

        for ((i, char) in arrangement.withIndex()) {
            if (char != '?') continue

            possibleArrangements = possibleArrangements
                .flatMap { arr ->
                    if (arr.count { it == '#' } < maxReplacement) listOf(
                        arr.replaceRange(i, i + 1, "."),
                        arr.replaceRange(i, i + 1, "#")
                    ) else listOf(
                        arr.replace('?', '.')
                    )
                }.filter { arr ->
                    (arr.any { it == '?' } && arr.count { it == '#' } <= maxReplacement) || arr.count { it == '#' } == maxReplacement
                }.filter {
                    var groupIndex = 0
                    var groupSize = 0
                    for (c in it) {
                        when (c) {
                            '?' -> return@filter true
                            '#' -> groupSize++
                            else -> if (groupSize != 0) {
                                if (groupSize != groups[groupIndex]) return@filter false
                                groupIndex++
                                groupSize = 0
                            }
                        }
                        if (groupIndex < groups.size && groupSize > groups[groupIndex]) return@filter false
                    }
                    return@filter true
                }
//                .also { colorPrint("$it\n", EscapeSequence.Color.Green) }
        }
        return possibleArrangements
    }

    fun generatePossibilities(): List<String> {
        // Pre-parsing, obvious solutions
        if (arrangement.none { it == '?' }) return listOf(arrangement)
//    else if ((arrangement to groups) in knownSolutions) {
//        return knownSolutions[arrangement to groups]!!
//    }

        // If we have separations, split it, process it and combine it after
        val possibilities = if (arrangement.any { it == '.' }) {
            val split = groupOrSeparatorRegex.findAll(arrangement)
                .map { it.destructured.component1() }
                .toList()

            val potentialGroups = mutableListOf<SpringGroup>()
            for ((i, string) in split.withIndex()) {
                if (string.any { it != '.' }) {
                    potentialGroups.add(
                        SpringGroup(
                            string,
                            intArrayOf(),
                            if (i > 0) split[i - 1] else "",
                            if (i < split.lastIndex) split[i + 1] else "",
                        )
                    )
                }
            }

            if (potentialGroups.size > groups.size && groups.all { group -> potentialGroups.all { group <= it.arrangement.length } }) {
                goThroughCombinations()
            } else {
                val unplacedGroups = groups.toMutableList()
                val unsortedGroups = potentialGroups.map { potentialGroup ->
                    if (unplacedGroups.isEmpty()) potentialGroup
                    else {
                        var neededSize = unplacedGroups.first()

                        val placedGroups = mutableListOf<Int>()
                        while (neededSize <= potentialGroup.arrangement.length) {
                            placedGroups += unplacedGroups.removeFirst()
                            if (unplacedGroups.isEmpty()) break
                            neededSize += unplacedGroups.first() + 1
                        }
                        potentialGroup.copy(groups = placedGroups.toIntArray())
                    }
                }

                val unfilteredGroups = unsortedGroups.toMutableList()
                val filteredGroups = mutableListOf<SpringGroup>()

                while (unfilteredGroups.isNotEmpty()) {
                    val springGroup = unfilteredGroups.removeLast()
                    val filteredSpacePerGroup =
                        if (springGroup.groups.isEmpty() && unfilteredGroups.isEmpty()) {
                            springGroup.copy(arrangement = springGroup.arrangement.replace('?', '.'))
                        } else if (springGroup.groups.isEmpty() && unfilteredGroups.last().groups.any { springGroup.arrangement.length >= it }) {
                            val concatenatedGroups = mutableListOf(springGroup)

                            var availableSpaceRight: Int
                            var availableSpaceLeft: Int
                            do {
                                val previousSpringGroup = unfilteredGroups.removeLast()
                                concatenatedGroups.add(0, previousSpringGroup)

                                availableSpaceRight = previousSpringGroup.arrangement.length
                                availableSpaceLeft = unfilteredGroups.lastOrNull()
                                    ?.let { (arr, _) -> arr.length }
                                    ?: 0
                            } while (
                                unfilteredGroups.isNotEmpty() &&
                                unfilteredGroups.last().groups.isNotEmpty() &&
                                unfilteredGroups.last().groups.last() <= availableSpaceRight && // last of left group can fit to the right
                                concatenatedGroups.first().groups.first() <= availableSpaceLeft  // first of right group can fit to the left
                            )

                            concatenatedGroups.reduce { acc, group -> acc + group }
                        } else springGroup
                    filteredGroups.add(0, filteredSpacePerGroup)
                }

                if (filteredGroups.size < unsortedGroups.size && groups.all { group -> potentialGroups.all { group <= it.arrangement.length } }) {
                    return goThroughCombinations()
                }

                val possibilitiesPerGroup = filteredGroups.map {
                    it to if (it.arrangement == arrangement) it.goThroughCombinations()
                    else it.generatePossibilities()
                }

                if (possibilitiesPerGroup.any { it.second.isEmpty() }) {
                    goThroughCombinations()
                } else {
                    val lastSeparator = possibilitiesPerGroup.last().let { (group, _) -> group.nextSeparator }
                    possibilitiesPerGroup.fold(listOf("")) { acc, (group, possibilities) ->
                        acc
                            .map { it + group.previousSeparator }
                            .flatMap { part -> possibilities.map { part + it } }
                    }.map { it + lastSeparator }
                }
            }
        } else { // Only one group, either perfect size or more space than needed
            val neededSize = groups.sum() + (groups.size - 1)
            val availableSpaces = arrangement.count { it == '?' || it == '#' }

            if (neededSize > availableSpaces) {
                error("More space needed than available")
            } else if (neededSize == availableSpaces) {
                // We just need to replace the ? by #
                var newArrangement = ""
                var unplacedGroupIndex = 0
                var groupCount = 0
                for (c in arrangement) {
                    if (c == '#') {
                        newArrangement += c
                        groupCount++
                    } else if (groupCount < groups[unplacedGroupIndex]) {
                        newArrangement += '#'
                        groupCount++
                    } else {
                        newArrangement += '.'
                        unplacedGroupIndex++
                        groupCount = 0
                    }
                }
                listOf(newArrangement)
            } else {
                // Complete the not obvious solutions
                goThroughCombinations()
            }
        }

//    knownSolutions[arrangement to groups] = possibilities
        return possibilities
    }
}

val groupOrSeparatorRegex = Regex("(([#?]+)|([.]+))")

fun main() {
    fun parse(input: List<String>, foldFactor: Int = 1): List<SpringGroup> {
        return input.map { line ->
            line.split(' ').let { (arrangement, groups) ->
                val parsedGroup = groups.split(',').map { it.toInt() }
                val unfoldedArrangement = (1..foldFactor).joinToString("?") { arrangement }
                val unfoldedGroups = (1..foldFactor).flatMap { parsedGroup }.toIntArray()

                SpringGroup(unfoldedArrangement, unfoldedGroups)
            }
        }
    }

    fun part1(input: List<String>): Int {
        return parse(input).sumOf { it.generatePossibilities().size }
    }

    fun part2(input: List<String>): Int {
        val total = input.size
        val progress = AtomicInteger(0)

        println("Starting...")
        val possibilities = parse(input, 5)
            .parallelStream()
            .map { springGroup ->
                val possibilities = springGroup.generatePossibilities()
                clearLine()
                colorPrint("${progress.incrementAndGet()} out of $total", EscapeSequence.Color.Green)
                possibilities
            }.toList()
        println()

        return possibilities.sumOf { it.size }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day12_test")
    checkEqual(part1(testInput), 21)
    checkEqual(part2(listOf(".??..??...?##. 1,1,3")), 16384)
    checkEqual(part2(testInput), 525152)

    // apply on real input
    val input = readInput("Day12")
//    boldPrint("\nPart 1: ${part1(input)}\n")
//    boldPrint("\nPart 2: ${part2(input)}\n")
}
