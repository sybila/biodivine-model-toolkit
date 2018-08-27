package biodivine.model.parser.token

import biodivine.model.parser.Rule
import biodivine.model.parser.RuleId
import biodivine.model.parser.Token

sealed class Identifier(override val id: String) : Rule {
    // All identifiers are parsed using the same regex. They are differentiated based on type inference
    // and cross-referencing with keywords later.

    companion object {
        // Identifiers always start with a letter and continue with an alphanumeric string or _
        val IDENTIFIER_REGEX = Regex("@?[a-zA-Z][a-zA-Z0-9_]*")
    }

    override fun scanToken(line: String, position: Int): Token? =
            matchRegex(line, position, IDENTIFIER_REGEX)?.toToken(position)

    // There is a very important distinction between unspecified and unknown identifier.
    // Unspecified identifier has been parsed, but we were not able to determine its type/meaning.
    // Hence it can be anything, but it should not be considered an error.
    // However, unknown identifier is explicitly marked as missing/problematic and should be highlighted
    // as such.

    object Unspecified : Identifier(RuleId.Identifier.UNSPECIFIED)
    object Unknown : Identifier(RuleId.Identifier.UNKNOWN)

}