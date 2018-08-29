package biodivine.model.parser.token

import biodivine.model.parser.ExactRule
import biodivine.model.parser.Rule
import biodivine.model.parser.RuleId
import biodivine.model.parser.Token

/**
 * Return a substring (or null if empty) of characters matching [condition] in [this] starting at [position].
 */
internal inline fun String.scanWhile(position: Int, condition: String.(Int, Char) -> Boolean): String? {
    var i = position
    while (i < length && condition(i, this[i])) i += 1
    return if (i == position) null else substring(position until i)
}

/**
 * Try to match regex starting at [position]. If unsuccessful, return null.
 */
internal fun matchRegex(line: String, position: Int, regex: Regex): String? {
    val match = regex.find(line, position)
    return if (match == null || match.range.first != position) null else {
        line.substring(match.range)
    }
}


object Whitespace : Rule {

    override val id: String = RuleId.WHITESPACE

    override fun scanToken(line: String, position: Int): Token? =
            line.scanWhile(position) { _, c -> c.isWhitespace() && c != '\n' }?.toToken(position)

}

object NewLine : ExactRule {
    override val id: String = RuleId.NEW_LINE
    override val value: String = "\n"
}

object External : ExactRule {
    override val id: String = RuleId.EXTERNAL
    override val value: String = "external"
}

object In : ExactRule {
    override val id: String = RuleId.IN
    override val value: String = "in"
}
