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

fun LongRange.toStream(): LongStream {
    return if (step == 1L) LongStream.rangeClosed(first, last)
    else asSequence().asStream().mapToLong { it }
}

fun IntRange.toStream(): IntStream {
    return if (step == 1) IntStream.rangeClosed(first, last)
    else asSequence().asStream().mapToInt { it }
}
