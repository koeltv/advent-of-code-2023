data class WastelandMap(val instructions: List<Char>, val nodes: Map<String, Pair<String, String>>) {
    fun countSteps(from: String, stopCondition: (String) -> Boolean): Int {
        var steps = 0
        var currentNode = from
        while (!stopCondition(currentNode)) {
            currentNode =
                if (instructions[steps % instructions.size] == 'L') nodes[currentNode]!!.first
                else nodes[currentNode]!!.second
            steps++
        }
        return steps
    }

    companion object {
        fun parseFrom(input: List<String>): WastelandMap {
            val instructions = input[0].toList()

            val nodes = input.drop(2).associate {
                val (sourceNode, leftNode, rightNode) = Regex("(\\w+) = \\((\\w+), (\\w+)\\)").find(it)!!.destructured
                sourceNode to (leftNode to rightNode)
            }

            return WastelandMap(instructions, nodes)
        }
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        val map = WastelandMap.parseFrom(input)

        return map.countSteps("AAA") { it == "ZZZ" }
    }

    fun part2(input: List<String>): Long {
        val map = WastelandMap.parseFrom(input)

        return map.nodes.keys
            .filter { it.endsWith('A') }
            .map { startingNode -> map.countSteps(startingNode) { it.endsWith('Z') } }
            .lcm()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 2)

    val testInput2 = readInput("Day08_test2")
    check(part2(testInput2) == 6L)

    // apply on real input
    val input = readInput("Day08")
    part1(input).println()
    part2(input).println()
}