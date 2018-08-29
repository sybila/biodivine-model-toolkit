package biodivine.model.parser.token

import biodivine.model.parser.ExactRule
import biodivine.model.parser.RuleId
import biodivine.model.parser.Tokenizer
import biodivine.model.parser.RuleId.Declaration as Id

/** See [RuleId] and [Tokenizer] for more info about token rules. */
sealed class Declaration(override val value: String, override val id: String) : ExactRule {

    object Constant : Declaration("const", Id.CONST)
    object Function : Declaration("fun", Id.FUN)
    object Enum : Declaration("enum", Id.ENUM)
    object Parameter : Declaration("param", Id.PARAM)
    object Variable : Declaration("var", Id.VAR)
    object Event : Declaration("event", Id.EVENT)

}