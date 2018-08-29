package biodivine.model.parser.token

import biodivine.model.parser.Rule
import biodivine.model.parser.RuleId
import biodivine.model.parser.Token
import biodivine.model.parser.Tokenizer
import biodivine.model.parser.RuleId.Identifier as Id

/** See [RuleId] and [Tokenizer] for more info about token rules. */
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

    object Unspecified : Identifier(Id.UNSPECIFIED)
    object Unknown : Identifier(Id.MISSING)

    object Constant : Identifier(Id.CONSTANT)
    object Function : Identifier(Id.FUNCTION)
    object Variable : Identifier(Id.VARIABLE)
    object Parameter : Identifier(Id.PARAMETER)
    object Enum : Identifier(Id.ENUM)
    object EnumValue : Identifier(Id.ENUM_VALUE)
    object Annotation : Identifier(Id.ANNOTATION)
    object ExternalConstant : Identifier(Id.E_CONSTANT)
    object ExternalFunction : Identifier(Id.E_FUNCTION)

}