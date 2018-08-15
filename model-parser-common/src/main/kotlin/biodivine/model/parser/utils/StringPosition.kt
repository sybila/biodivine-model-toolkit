package biodivine.model.parser.utils

/**
 * Position at given [index] in a string. To a human, this is given by [line] and [char] in that line.
 */
data class StringPosition(
        val line: Int,
        val char: Int,
        val index: Int
)