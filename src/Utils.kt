import java.math.BigInteger
import java.security.MessageDigest
import java.util.stream.IntStream
import java.util.stream.LongStream
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.streams.asStream

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)

/**
 * Merge 2 [Map] with the same structure.
 * This uses a merge function to process cases where a key is present in both maps
 *
 * @param map2
 * @param mergeFunction
 * @receiver
 * @return
 */
fun <K, V> Map<K, V>.merge(map2: Map<K, V>, mergeFunction: (key: K, value1: V, value2: V) -> V): Map<K, V> {
    return this.filterKeys { it !in map2 } +
            map2.filterKeys { it !in this } +
            this.filterKeys { it in map2 }.mapValues { (key, value1) ->
                mergeFunction(key, value1, map2[key]!!)
            }
}

/**
 * Splits a list of elements on the given predicate, elements matching the predicate are thrown away.
 *
 * ```
 * val values = "1 2 3 4 5 6 7 8"
 * val usingSplit = values.split(" ")
 * val usingSplitOn = values.toList().splitOn { it == ' ' }.map { it.joinToString() }
 * ```
 *
 * @param T the type of elements in the list
 * @param predicate the predicate used to split the list
 * @receiver the list to be split
 * @return a list of lists, where each inner list represents a split sub-list
 */
fun <T> List<T>.splitOn(predicate: (T) -> Boolean): List<List<T>> =
    fold(mutableListOf(mutableListOf<T>())) { acc, element ->
        if (predicate(element)) acc.apply { add(mutableListOf()) }
        else acc.apply { last().add(element) }
    }

fun LongRange.toStream(): LongStream {
    return if (step == 1L) LongStream.rangeClosed(first, last)
    else asSequence().asStream().mapToLong { it }
}

fun IntRange.toStream(): IntStream {
    return if (step == 1) IntStream.rangeClosed(first, last)
    else asSequence().asStream().mapToInt { it }
}

/**
 * Calculates the least common multiple (LCM) of the integers in the list.
 *
 * @receiver the list of integers to calculate LCM for
 * @return the LCM of the integers in the list
 */
fun List<Int>.lcm(): Long {
    return fold(1L) { lcm, value ->
        (lcm * value) / gcd(lcm, value.toLong())
    }
}


/**
 * Calculates the greatest common divisor (GCD) of two numbers.
 *
 * @param a the first number
 * @param b the second number
 * @return the GCD of the two numbers
 */
fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)