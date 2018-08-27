package biodivine.model.parser.token

import biodivine.model.parser.ExactRule
import biodivine.model.parser.Rule
import biodivine.model.parser.RuleId

sealed class Operator(override val value: String, override val id: String) : ExactRule {

    object And : Operator("&&", RuleId.Operator.AND)

    object Or : Operator("||", RuleId.Operator.OR)

    object Not : Operator("!", RuleId.Operator.NOT)

    object Plus : Operator("+", RuleId.Operator.PLUS)

    object Minus : Operator("-", RuleId.Operator.MINUS)

    object Div : Operator("/", RuleId.Operator.DIV)

    object Mul : Operator("*", RuleId.Operator.MUL)

    object Greater : Operator(">", RuleId.Operator.GT)

    object GreaterEqual : Operator(">=", RuleId.Operator.GE)

    object Less : Operator("<", RuleId.Operator.LT)

    object LessEqual : Operator("<=", RuleId.Operator.LE)

    object Equal : Operator("==", RuleId.Operator.EQ)

    object NotEqual : Operator("!=", RuleId.Operator.NEQ)

}