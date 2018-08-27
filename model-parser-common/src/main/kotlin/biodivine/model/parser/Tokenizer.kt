package biodivine.model.parser

import biodivine.model.parser.token.*

object Tokenizer {

    sealed class State {
        object Normal: State()
        object LineComment : State()
        object String : State()
        data class BlockComment(val level: Int) : State()
    }

    fun scanTokens(line: String, state: State): Pair<List<Token>, State> {
        val result = ArrayList<Token>()
        var position = 0
        var currentState = state
        while (position < line.length) {
            val (token, nextState) = scanToken(line, position, currentState)
            position += token.value.length
            currentState = nextState
            result.add(token)
        }
        return result to currentState
    }

    private val scanRules = listOf(
            Comment.Block.Open, Comment.Block.Close,                    // /* */
            Comment.Line.StartC, Comment.Line.StartPython,              // // #
            Misc.Then, Misc.Dot,                                        // -> .
            Operator.Less, Operator.LessEqual, Operator.NotEqual,       // < <= !=
            Operator.Greater, Operator.GreaterEqual, Operator.Equal,    // > >= ==
            Operator.And, Operator.Or, Operator.Not,                    // && || !
            Operator.Plus, Operator.Minus, Operator.Div, Operator.Mul,  // + - / *
            Misc.ParOpen, Misc.ParClose, Misc.Comma,                    // ( ) ,
            Misc.BlockOpen, Misc.BlockClose, Misc.Whitespace,           // { } \s
            Misc.BracketOpen, Misc.BracketClose,                        // [ ]
            Literal.Text.Open, Misc.Assign, Misc.Range,                 // " = ..
            Literal.Number, Identifier.Unspecified                      // 123 ???
    )

    private val keywords: List<ExactRule> = listOf(
            Literal.True, Literal.False, Keyword.In, Keyword.When, Keyword.Var, Keyword.Event,
            Keyword.Const, Keyword.External, Keyword.Function, Keyword.Enum, Keyword.Param
    )

    fun scanToken(line: String, position: Int, state: State): Pair<Token, State> {
        if (position < 0 || position >= line.length) {
            error("Invalid position $position in line of length ${line.length}")
        }
        return when (state) {
            State.LineComment -> (Comment.Line.Value.scanToken(line, position) ?: unreachable()) to State.Normal
            is State.BlockComment -> {
                Comment.Block.Open.scanToken(line, position)?.let {
                    it to State.BlockComment(state.level + 1)
                } ?:
                Comment.Block.Close.scanToken(line, position)?.let {
                    it to (if (state.level > 0) State.BlockComment(state.level - 1) else State.Normal)
                } ?:
                Comment.Block.Value.scanToken(line, position)?.let {
                    it to state
                } ?: unreachable()
            }
            State.String -> {
                Literal.Text.Close.scanToken(line, position)?.let {
                    it to State.Normal
                } ?:
                Literal.Text.EscapeChar.scanToken(line, position)?.let {
                    it to State.String
                } ?:
                Literal.Text.Value.scanToken(line, position)?.let {
                    it to State.String
                } ?: unreachable()
            }
            State.Normal -> {
                val rawToken = scanRules.asSequence()
                        .map { it.scanToken(line, position) }
                        .filterNotNull().firstOrNull()

                // Since keywords collide with identifiers, we handle them explicitly:
                val token: Token? = if (rawToken?.rule !is Identifier) rawToken else {
                    keywords.find { it.value == rawToken.value }?.let { rawToken.copy(rule = it) } ?:
                        makeIf(rawToken.value.startsWith('@')) { rawToken.copy(rule = Identifier.Annotation) } ?:
                        rawToken
                }

                val nextState = when (token?.rule) {
                    Comment.Line.StartC, Comment.Line.StartPython -> State.LineComment
                    Comment.Block.Open -> State.BlockComment(0)
                    Literal.Text.Open -> State.String
                    else -> state
                }

                if (token == null) {
                    // unknown token
                    Token(null, line.substring(position, position + 1), position) to state
                } else {
                    token to nextState
                }
            }
        }
    }

}