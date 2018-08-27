package biodivine.model.parser

/**
 * Token is an atomic unit of text. It is identified by a rule (if null, token type is unknown) and the
 * actual value.
 */
data class Token(
        val rule: Rule?, val value: String, val startsAt: Int
)