package biodivine.model.parser

/**
 * Exact rules are special types of rules which only match a specific string.
 */
interface ExactRule : Rule {

    /**
     * The value matched by this rule.
     */
    val value: String

    override fun scanToken(text: String, position: Int): Token? = makeIf(text.startsWith(value, position)) {
        Token(this, value, position)
            }

}