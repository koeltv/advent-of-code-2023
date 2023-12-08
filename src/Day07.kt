import kotlin.math.max

data class Card(val symbol: Char, val lowJokers: Boolean = false) : Comparable<Card> {
    override fun compareTo(other: Card): Int {
        return if (lowJokers) lowJokerOrdering.indexOf(symbol) - lowJokerOrdering.indexOf(other.symbol)
        else standardOrdering.indexOf(symbol) - standardOrdering.indexOf(other.symbol)
    }

    companion object {
        private val standardOrdering = listOf('2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A')
        private val lowJokerOrdering = listOf('J', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'Q', 'K', 'A')
    }
}

enum class Category {
    HighCard,
    OnePair,
    TwoPairs,
    ThreeOfAKind,
    FullHouse,
    FourOfAKind,
    FiveOfAKind
}

data class Hand(val cards: List<Card>, private val useJokers: Boolean = false) : Comparable<Hand> {
    private val category: Category = run {
        // This implementation could cause problem when best solution requires jokers to have different values
        if (useJokers) {
            val distinctCards = cards.distinct()
            if (distinctCards.any { it.symbol == 'J' } && distinctCards.size > 1) {
                // We change the jokers for other existing symbols and keep the best combination
                return@run distinctCards.filter { it.symbol != 'J' }.maxOf { distinctCard ->
                    Hand(cards.map { if (it.symbol == 'J') distinctCard else it })
                }.let { categoryOf(it) }
            }
        }
        categoryOf(this)
    }

    companion object {
        private fun categoryOf(hand: Hand): Category {
            val distinctCards = hand.cards.distinct()

            return when (distinctCards.count()) {
                1 -> Category.FiveOfAKind
                2 -> {
                    distinctCards
                        .map { hand.cards.count { card -> card == it } }
                        .let { (card1Count, card2Count) ->
                            if (max(card1Count, card2Count) == 4) Category.FourOfAKind
                            else Category.FullHouse
                        }
                }
                3 -> {
                    if (distinctCards.any { hand.cards.count { card -> card == it } == 3 }) Category.ThreeOfAKind
                    else Category.TwoPairs
                }
                4 -> Category.OnePair
                5 -> Category.HighCard
                else -> throw IllegalArgumentException("Hand has ${hand.cards.size} cards")
            }
        }
    }

    override fun compareTo(other: Hand): Int {
        return if (category < other.category) -1
        else if (category > other.category) 1
        else {
            for ((card, otherCard) in cards.zip(other.cards)) {
                if (card < otherCard) return -1
                else if (card > otherCard) return 1
            }
            0
        }
    }
}

fun main() {
    fun calculateTotalWinnings(handsWithBid: Map<Hand, Int>) = handsWithBid.keys
        .sorted()
        .mapIndexed { i, hand -> i + 1 to hand }
        .sumOf { (rank, hand) -> rank * handsWithBid[hand]!! }

    fun part1(input: List<String>): Int {
        val handsWithBid = input.associate { line ->
            val (symbols, bid) = line.split(" ")
            Hand(symbols.map { Card(it) }) to bid.toInt()
        }

        return calculateTotalWinnings(handsWithBid)
    }

    fun part2(input: List<String>): Int {
        val handsWithBid = input.associate { line ->
            val (symbols, bid) = line.split(" ")
            Hand(symbols.map { Card(it, lowJokers = true) }, useJokers = true) to bid.toInt()
        }

        return calculateTotalWinnings(handsWithBid)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 6440)

    check(part2(testInput) == 5905)

    // apply on real input
    val input = readInput("Day07")
    part1(input).println()
    part2(input).println()
}
