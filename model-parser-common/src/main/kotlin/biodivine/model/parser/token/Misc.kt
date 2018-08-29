package biodivine.model.parser.token

import biodivine.model.parser.*
import biodivine.model.parser.RuleId.Misc as Id

/** See [RuleId] and [Tokenizer] for more info about token rules. */
sealed class Misc(override val value: String, override val id: String) : ExactRule {

    object Comma : Misc(",", Id.COMMA)
    
    object ParOpen : Misc("(", Id.PAR_OPEN)
    object ParClose : Misc(")", Id.PAR_CLOSE)
    
    object BlockOpen : Misc("{", Id.BLOCK_OPEN)
    object BlockClose : Misc("}", Id.BLOCK_CLOSE)

    object BracketOpen : Misc("[", Id.BRACKET_OPEN)
    object BracketClose : Misc("]", Id.BRACKET_CLOSE)

    object Assign : Misc("=", Id.ASSIGN)

    object Range : Misc("..", Id.RANGE)

    object Then : Misc("->", Id.THEN)

    object Dot : Misc(".", Id.DOT)
    
    object Case : Misc("|", Id.CASE)

}