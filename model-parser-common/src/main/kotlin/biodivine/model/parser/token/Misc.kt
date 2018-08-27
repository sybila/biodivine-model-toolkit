package biodivine.model.parser.token

import biodivine.model.parser.ExactRule
import biodivine.model.parser.Rule
import biodivine.model.parser.RuleId
import biodivine.model.parser.Token

sealed class Misc(override val id: String) : Rule {

    object Comma : Misc(RuleId.Misc.COMMA), ExactRule {
        override val value: String = ","
    }

    object ParOpen : Misc(RuleId.Misc.PAR_OPEN), ExactRule {
        override val value: String = "("
    }

    object ParClose : Misc(RuleId.Misc.PAR_CLOSE), ExactRule {
        override val value: String = ")"
    }

    object BlockOpen : Misc(RuleId.Misc.BLOCK_OPEN), ExactRule {
        override val value: String = "{"
    }

    object BlockClose : Misc(RuleId.Misc.BLOCK_CLOSE), ExactRule {
        override val value: String = "}"
    }

    object BracketOpen : Misc(RuleId.Misc.BRACKET_OPEN), ExactRule {
        override val value: String = "["
    }

    object BracketClose : Misc(RuleId.Misc.BRACKET_CLOSE), ExactRule {
        override val value: String = "]"
    }

    object Assign : Misc(RuleId.Misc.ASSIGN), ExactRule {
        override val value: String = "="
    }

    object Range : Misc(RuleId.Misc.RANGE), ExactRule {
        override val value: String = ".."
    }

    object Then : Misc(RuleId.Misc.THEN), ExactRule {
        override val value: String = "->"
    }

    object Whitespace : Misc(RuleId.Misc.WHITESPACE) {
        override fun scanToken(line: String, position: Int): Token? =
                line.scanWhile(position) { _, c -> c.isWhitespace() }?.toToken(position)
    }

}