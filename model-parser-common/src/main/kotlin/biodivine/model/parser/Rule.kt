package biodivine.model.parser

/**
 * Rule is an object which tries to match a specific type of token.
 *
 * Note that the rules are not mutually exclusive (const is readable as an
 * identifier, but it is actually a keyword, etc.). It is therefore important to
 * apply the rules in the correct order.
 *
 * Furthermore, not all rules are applicable always - a block comment value can be read only after encountering
 * block comment start, etc. - so the tokenizer needs to maintain an internal state to track this.
 *
 * No token spans multiple lines.
 */
sealed class Rule(
        val id: String
) {

    /**
     * Try to scan a token defined by this rule in [line] at given [position].
     *
     * If the token does not match (or the position is invalid), return null.
     *
     * Note that the next scanning position can be determined by using the length of [Token.value].
     */
    abstract fun scanToken(line: String, position: Int): Token?

    /**
     * A helper function which bundles a string into a token object using the current rule and a given position.
     */
    protected fun String.toToken(at: Int): Token = Token(this@Rule, this, at)
}

/**
 * RuleIds are strings which identify the rules outside of the Kotlin runtime (CSS rules in highlighter, etc.).
 */
object RuleId {

    object Identifier {
        const val UNKNOWN = "id_unknown"
    }

    object Literal {
        const val NUMBER = "literal_number"
        const val BOOLEAN = "literal_boolean"
        object String {
            const val OPEN = "str_open"
            const val CLOSE = "str_close"
            const val ESCAPE_CHAR = "str_escape"
            const val VALUE = "str_value"
        }
        object Array {
            const val OPEN = "array_open"
            const val CLOSE = "array_close"
        }
    }

    object Comment {
        object Block {
            const val OPEN = "comment_block_open"
            const val CLOSE = "comment_block_close"
            const val VALUE = "comment_block_value"
        }
        object Line {
            const val START_C = "comment_line_c"
            const val START_PYTHON = "comment_line_python"
            const val VALUE = "comment_line_value"
        }
    }

}

/**
 * Exact rules match only a specific string and can be therefore handled trivially (see [matchExact]).
 */
private interface ExactRule {
    val value: String
}

private fun <R> R.matchExact(line: String, position: Int): Token? where R: Rule, R: ExactRule =
        if (!line.startsWith(value, startIndex = position)) null else {
            Token(this, value, position)
        }

/**
 * Return a substring (or null if empty) of characters matching [condition] in [this] starting at [position].
 */
private inline fun String.scanWhile(position: Int, condition: String.(Int, Char) -> Boolean): String? {
    var i = position
    while (i < length && condition(i, this[i])) i += 1
    return if (i == position) null else substring(position until i)
}

/**
 * Try to match regex starting at [position]. If unsuccessful, return null.
 */
private fun matchRegex(line: String, position: Int, regex: Regex): String? {
    val match = regex.find(line, position)
    return if (match == null || match.range.first != position) null else {
        line.substring(match.range)
    }
}

sealed class Comment(id: String) : Rule(id) {

    sealed class Block(id: String) : Comment(id) {

        object Open : Block(RuleId.Comment.Block.OPEN), ExactRule {
            override val value: String = "/*"
            override fun scanToken(line: String, position: Int): Token? = matchExact(line, position)
        }

        object Close : Block(RuleId.Comment.Block.CLOSE), ExactRule {
            override val value: String = "*/"
            override fun scanToken(line: String, position: Int): Token? = matchExact(line, position)
        }

        object Value : Block(RuleId.Comment.Block.VALUE) {
            override fun scanToken(line: String, position: Int): Token? {
                return line.scanWhile(position) { i, _ ->
                    Open.scanToken(line, i) == null && Close.scanToken(line, i) == null
                }?.toToken(position)
            }
        }

    }

    sealed class Line(id: String) : Comment(id) {

        object StartC : Line(RuleId.Comment.Line.START_C), ExactRule {
            override val value: String = "//"
            override fun scanToken(line: String, position: Int): Token? = matchExact(line, position)
        }

        object StartPython : Line(RuleId.Comment.Line.START_PYTHON), ExactRule {
            override val value: String = "#"
            override fun scanToken(line: String, position: Int): Token? = matchExact(line, position)
        }

        object Value : Line(RuleId.Comment.Line.VALUE) {
            override fun scanToken(line: String, position: Int): Token? {
                return line.substring(position).takeIf { it.isNotEmpty() }?.toToken(position)
            }
        }

    }

}

sealed class Identifier(id: String) : Rule(id) {
    // All identifiers are parsed using the same regex. They are differentiated based on type inference
    // and cross-referencing with keywords later.

    companion object {
        // Identifiers always start with a letter and continue with an alphanumeric string or _
        val IDENTIFIER_REGEX = Regex("@?[a-zA-Z][a-zA-Z0-9_]*")
    }

    override fun scanToken(line: String, position: Int): Token? =
            matchRegex(line, position, IDENTIFIER_REGEX)?.toToken(position)

    object Unknown : Identifier(RuleId.Identifier.UNKNOWN)

}

sealed class Literal(id: String) : Rule(id) {

    companion object {
        // Neither real nor int literals can be negative. The minus sign is treated as unary operator instead.

        // Real literals must contain a decimal dot and optionally also scientific notation (1e-4)
        val REAL_LITERAL_REGEX = Regex("\\d+(?:\\.\\d+)?(?:[eE]-?\\d+)?")

        // Int literals are just numbers
        val INT_LITERAL_REGEX = Regex("\\d+")

    }

    object Real : Literal(RuleId.Literal.REAL) {
        override fun scanToken(line: String, position: Int): Token? =
                matchRegex(line, position, REAL_LITERAL_REGEX)?.toToken(position)
    }

    object Integer : Literal(RuleId.Literal.INT) {
        override fun scanToken(line: String, position: Int): Token? =
                matchRegex(line, position, INT_LITERAL_REGEX)?.toToken(position)
    }

    object True : Literal(RuleId.Literal.TRUE), ExactRule {
        override val value: String = "true"
        override fun scanToken(line: String, position: Int): Token? = matchExact(line, position)
    }

    object False : Literal(RuleId.Literal.FALSE), ExactRule {
        override val value: String = "false"
        override fun scanToken(line: String, position: Int): Token? = matchExact(line, position)
    }

    sealed class Text(id: String) : Literal(id) {

        object Open : Text(RuleId.Literal.String.OPEN), ExactRule {
            override val value: String = "\""
            override fun scanToken(line: String, position: Int): Token? = matchExact(line, position)
        }

        object Close : Text(RuleId.Literal.String.CLOSE), ExactRule {
            override val value: String = "\""
            override fun scanToken(line: String, position: Int): Token? = matchExact(line, position)
        }

        object Escape : Text(RuleId.Literal.String.ESCAPE) {
            override fun scanToken(line: String, position: Int): Token? {
                // we need to be at least two characters before the end of the line and the first needs to be \
                return if (position >= line.length - 1 || line[position] != '\\') null else {
                    line.substring(position, position + 2).toToken(position)
                }
            }
        }

        object Value : Text(RuleId.Literal.String.VALUE) {
            override fun scanToken(line: String, position: Int): Token? {
                return line.scanWhile(position) { _, c -> c != '"' && c != '\\' }?.toToken(position)
            }
        }

    }


}