package biodivine.model.editor

import ace.Tokenizer
import biodivine.model.parser.Tokenizer as ModelTokenizer
import biodivine.model.parser.Tokenizer.State as TokenizerState

object AceTokenizer : Tokenizer<AceTokenizer.Token, TokenizerState>() {

    data class Token(override val type: String, override val value: String) : Tokenizer.Token

    class Tokens(override val state: TokenizerState?,
                 override val tokens: Array<Token>
    ) : Tokenizer.Tokens<AceTokenizer.Token, TokenizerState>

    override fun getLineTokens(line: String, startState: TokenizerState?): Tokens {
        val (tokens, nextState) = ModelTokenizer.scanTokens(line, startState ?: TokenizerState.Normal)
        return Tokens(nextState, tokens.map { Token(it.rule?.id ?: "unknown", it.value) }.toTypedArray())
    }

}