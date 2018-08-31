package biodivine.model.parser

/**
 * Rule is an object which tries to match a specific type of token.
 *
 * Note that the rules are not mutually exclusive (const is readable as an
 * identifier, but it is actually a keyword, etc.). It is therefore important to
 * apply the rules in the correct order.
 *
 * Furthermore, not all rules are applicable always - a block comment value can be read only after encountering
 * block comment start, etc. - so the tokenizer needs to maintain an internal state to track this.
 *
 * No token spans multiple lines.
 */
interface Rule {

    /**
     * The string identifier of this rule. It is used by external tools to identify rules.
     *
     * We don't use a class name, because it can change (refactoring), or it can be incompatible
     * with the external ID system.
     */
    val id: String

    /**
     * Try to scan a token defined by this rule in [text] at given [position].
     *
     * If the token does not match (or the position is invalid), return null.
     *
     * Note that the next scanning position can be determined by using the length of [Token.value].
     */
    fun scanToken(text: String, position: Int): Token?

    /**
     * A helper function which bundles a string into a token object using the current rule and a given position.
     */
    fun String.toToken(at: Int): Token = Token(this@Rule, this, at)
}