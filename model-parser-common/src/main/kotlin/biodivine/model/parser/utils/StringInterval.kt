package biodivine.model.parser.utils

/**
 * An interval in a string, given by first and last position (inclusive).
 */
data class StringInterval(
        val first: StringPosition, val last: StringPosition
)