package biodivine.model.parser.token

import biodivine.model.parser.*
import biodivine.model.parser.RuleId.Literal as Id

/** See [RuleId] and [Tokenizer] for more info about token rules. */
sealed class Literal(override val id: String) : Rule {

    companion object {
        // Number literal can be any number with optional decimal places and scientific notation (1.56e-32)
        val NUMBER_LITERAL_REGEX = Regex("\\d+(?:\\.\\d+)?(?:[eE]-?\\d+)?")

    }

    object Number : Literal(Id.NUMBER) {
        override fun scanToken(text: String, position: Int): Token? =
                matchRegex(text, position, NUMBER_LITERAL_REGEX)?.toToken(position)
    }

    object True : Literal(Id.TRUE), ExactRule {
        override val value: String = "true"
    }

    object False : Literal(Id.FALSE), ExactRule {
        override val value: String = "false"
    }

    sealed class Text(id: String) : Literal(id) {

        object Quote : Text(Id.Text.QUOTE), ExactRule {
            override val value: String = "\""
        }

        object EscapeChar : Text(RuleId.Literal.Text.ESCAPE_CHAR) {

            override fun scanToken(text: String, position: Int): Token? {
                // we need to be at least two characters before the end of the text and the first needs to be \
                return makeIf(position < text.length - 1 && text[position] == '\\') {
                    text.substring(position, position + 2).toToken(position)
                }
            }

        }

        object Value : Text(RuleId.Literal.Text.VALUE) {

            override fun scanToken(text: String, position: Int): Token? {
                return text.scanWhile(position) { _, c -> c != '"' && c != '\\' }?.toToken(position)
            }

        }

    }

}