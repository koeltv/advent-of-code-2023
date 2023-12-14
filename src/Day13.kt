import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

/**
 * Find mirrors and report counts of rows and columns before them.
 * We need to report lists here because we may have more than 1 mirror in the rows/columns,
 * and we want to pick a different one than the first we found.
 *
 * @param pattern
 * @return
 */
fun findMirrorsAndReportCounts(pattern: List<String>): Pair<List<Int>, List<Int>> {
    val rowsAboveMirror = (0..<pattern.lastIndex).mapNotNull { rowIndex ->
        val mirrorRange = 0..min(rowIndex, pattern.lastIndex - rowIndex - 1)
        val isMirrored = mirrorRange.all { dy ->
            pattern[rowIndex - dy] == pattern[rowIndex + 1 + dy]
        }
        if (isMirrored) rowIndex + 1 else null
    }
    val columnsBeforeMirror = (0..<pattern[0].lastIndex).mapNotNull { columnIndex ->
        val mirrorRange = 0..min(columnIndex, pattern[0].lastIndex - columnIndex - 1)
        val isMirrored = mirrorRange.all { dy ->
            pattern.all { it[columnIndex - dy] == it[columnIndex + 1 + dy] }
        }
        if (isMirrored) columnIndex + 1 else null
    }
    return columnsBeforeMirror to rowsAboveMirror
}

private fun List<String>.tryToFix(coordinates: Coordinates): List<String> {
    val (targetX, targetY) = coordinates
    return mapIndexed { y, line ->
        line.mapIndexed { x, char ->
            if (x == targetX && y == targetY) {
                if (char == '#') '.' else '#'
            } else {
                char
            }
        }.joinToString("")
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        val (columnsBeforeMirror, rowsAboveMirror) = input
            .splitOn { it.isEmpty() }
            .fold(0 to 0) { (totalColumnCount, totalRowCount), pattern ->
                val (columnCount, rowCount) = findMirrorsAndReportCounts(pattern)
                totalColumnCount + (columnCount.firstOrNull() ?: 0) to totalRowCount + (rowCount.firstOrNull() ?: 0)
            }
        return columnsBeforeMirror + 100 * rowsAboveMirror
    }

    fun part2(input: List<String>): Int {
        val patterns = input.splitOn { it.isBlank() }

        val totalSize = patterns.size
        val progress = AtomicInteger(0)

        println()
        val (columnsBeforeMirror, rowsAboveMirror) = patterns
            .map { pattern ->
                val (initialColumnCount, initialRowCount) = findMirrorsAndReportCounts(pattern).let { (initialColumnsCount, initialRowsCount) ->
                    (initialColumnsCount.firstOrNull() ?: 0) to (initialRowsCount.firstOrNull() ?: 0)
                }

                var potentialSmudge = Coordinates(0, 0)
                do {
                    val newPattern = pattern.tryToFix(potentialSmudge)
                    val (columnCounts, rowCounts) = findMirrorsAndReportCounts(newPattern)

                    val newColumnCount = columnCounts.find { it != initialColumnCount }
                    val newRowCount = rowCounts.find { it != initialRowCount }
                    if (newColumnCount != null || newRowCount != null) {
                        progressBar(progress.incrementAndGet(), totalSize)
                        return@map (newColumnCount ?: 0) to (newRowCount ?: 0)
                    }

                    potentialSmudge = if (potentialSmudge.x < pattern[0].lastIndex) potentialSmudge.right()
                    else potentialSmudge.copy(x = 0, y = potentialSmudge.y + 1)
                } while (potentialSmudge in pattern)

                error("No smudge found")
            }
            .reduce { (a1, a2), (b1, b2) -> a1 + b1 to a2 + b2 }
        return columnsBeforeMirror + 100 * rowsAboveMirror
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day13_test")
    checkEqual(part1(testInput), 405)
    checkEqual(part2(testInput), 400)

// apply on real input
    val input = readInput("Day13")
    boldPrint("\nPart 1: ${part1(input)}\n")
    boldPrint("\nPart 2: ${part2(input)}\n")
}
