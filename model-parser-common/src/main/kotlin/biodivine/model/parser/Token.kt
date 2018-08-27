package biodivine.model.parser

data class Token(
        val rule: Rule, val value: String, val startsAt: Int
)