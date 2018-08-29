package biodivine.model.parser.token

import biodivine.model.parser.ExactRule
import biodivine.model.parser.RuleId
import biodivine.model.parser.Tokenizer
import biodivine.model.parser.RuleId.Operator as Id

/** See [RuleId] and [Tokenizer] for more info about token rules. */
sealed class Operator(override val value: String, override val id: String) : ExactRule {

    object And : Operator("&&", Id.AND)

    object Or : Operator("||", Id.OR)

    object Not : Operator("!", Id.NOT)

    object Plus : Operator("+", Id.PLUS)

    object Minus : Operator("-", Id.MINUS)

    object Div : Operator("/", Id.DIV)

    object Mul : Operator("*", Id.MUL)

    object Greater : Operator(">", Id.GT)

    object GreaterEqual : Operator(">=", Id.GE)

    object Less : Operator("<", Id.LT)

    object LessEqual : Operator("<=", Id.LE)

    object Equal : Operator("==", Id.EQ)

    object NotEqual : Operator("!=", Id.NEQ)

}