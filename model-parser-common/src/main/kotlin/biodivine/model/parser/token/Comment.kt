package biodivine.model.parser.token

import biodivine.model.parser.*
import biodivine.model.parser.RuleId.Comment as Id

/** See [RuleId] and [Tokenizer] for more info about token rules. */
sealed class Comment(override val id: String) : Rule {

    object BlockOpen : Comment(Id.BLOCK_OPEN), ExactRule {
        override val value: String = "/*"
    }

    object BlockClose : Comment(Id.BLOCK_CLOSE), ExactRule {
        override val value: String = "*/"
    }

    object StartC : Comment(Id.START_C), ExactRule {
        override val value: String = "//"
    }

    object StartPython : Comment(Id.START_PYTHON), ExactRule {
        override val value: String = "#"
    }

    object BlockValue : Comment(Id.BLOCK_VALUE) {

        override fun scanToken(text: String, position: Int): Token? =
                text.scanWhile(position) { i, c ->
                    BlockOpen.scanToken(text, i) == null && BlockClose.scanToken(text, i) == null && c != '\n'
                }?.toToken(position)

    }

    object LineValue : Comment(Id.LINE_VALUE) {

        override fun scanToken(text: String, position: Int): Token? =
                text.scanWhile(position) { _, c -> c != '\n' }?.toToken(position)

    }

}