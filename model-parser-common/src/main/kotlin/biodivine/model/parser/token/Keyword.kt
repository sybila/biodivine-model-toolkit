package biodivine.model.parser.token

import biodivine.model.parser.ExactRule
import biodivine.model.parser.RuleId

/**
 * Keyword tokens collide with identifiers. Hence, we parse every keyword as an identifier first, and then
 * map it to keyword if applicable.
 */
sealed class Keyword(override val value: String, override val id: String) : ExactRule {

    object External : Keyword("external", RuleId.Keyword.EXTERNAL)
    object Const : Keyword("const", RuleId.Keyword.CONST)
    object Function : Keyword("fun", RuleId.Keyword.FUN)
    object When : Keyword("when", RuleId.Keyword.WHEN)

}