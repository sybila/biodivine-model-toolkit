package biodivine.model.parser

import biodivine.model.parser.token.*

/**
 * BioDivine tokenizer processes an input string into a collection of [Token]s, which then can be analysed using
 * the parser into a complete syntax tree. Additionally, if enabled, the tokenizer can provide assumed types for each
 * reference (Note that these are primarily for syntax highlighting, as they don't have to be necessarily correct.
 * The parser decides the final type of each reference!).
 *
 * ### How does it work?
 *
 * The tokens are read based on [Rule]s, where each rule describes one type of token. The tokenizer passes through
 * the string, extracting one token after the other by iteratively trying to match different rules to the current
 * position. The first rule which matches is then used in the generated token.
 *
 * However, some rules can be conflicting (for example '->' and '-', or 'const' and identifier). To this end,
 * the tokenizer first extracts all explicit non-alphanumeric tokens (like '+', '-', '->', ...) in the order which
 * avoids conflicts (in general, the "bigger" token is extracted before the "smaller" one,
 * i.e. '->' is checked before '-').
 *
 * Next, numbers are extracted and and then identifiers (without type information). Finally, the identifiers are
 * checked to extract all keywords (So an identifier with value 'const' is replaced with the appropriate keyword).
 * It is performed in this way because identifier is actually a "bigger" token than the keywords ('constant' is an
 * identifier, not a keyword 'const' plus an identifier 'ant').
 *
 * Alternatively, we could first tokenize the non-alphanumeric tokens + whitespace and then extract keywords
 * in a "second pass" which will look ahead to see if the 'const' is the end of the alphanumeric sequence, but
 * that just seemed needlessly complicated at the time.
 *
 * The format is very permissive regarding whitespace. Technically, as long as you start a declaration on a new
 * line, everything else is up to you. This means the tokenizer batches all whitespace together,
 * except for the new-lines.
 *
 * If you want to check out the types of tokens this tokenizer works with, have a look at the [RuleId] object.
 *
 * ### What is the interface?
 *
 * The tokenizer has three "levels" of operation: Either you tokenize the whole file, a line in the file or you
 * read a single token at a specified position. This allows you to tokenize an update section without recomputing
 * most of the file.
 *
 * Note that there are also special new-line tokens, which are emitted for every new line encountered. They
 * are only emitted when they are present in the string (so if you use the line mode and you supply just the one line,
 * you won't get the new-line token at the end). However, in this case, the state will still be updated as if
 * the new line was there, since the tokenizer knows this is the end of the line.
 *
 * ### Tokenizer state
 *
 * In order to for each level to properly function (recognize strings, comments, etc.), the tokenizer needs
 * to maintain an internal state. Clearly, the 'const' in "a const in string" is part of the string and not
 * a keyword. To this end, the line and position levels accept the [Tokenizer.State] instance which captures
 * the last state before this position/line and return a state which is valid after scanning the position/line.
 *
 * But remember that if you tokenize the file in parts and the state for some position changes, you have to tokenize
 * the rest of the file until the states match again. Imagine adding a '/*' to the beginning of the file. All
 * lines until the next '*/' need to be re-scanned.
 *
 * The tokenizer state is implemented as stack in order to support nested comments and type hints (see below).
 *
 * ### Type hints
 *
 * In order to provide type hints for each reference, the tokenizer requires the global type information in the form
 * of a (string) -> identifier map. In theory this information could be also extracted by the tokenizer directly
 * (after you read declaration keyword, add the next identifier with this type). However, this is quite unpractical,
 * because the declarations can be arbitrarily ordered, so you would have to constantly re-tokenize everything.
 *
 * Instead, we assume the global types are provided by the type hinter which is typically run asynchronously in
 * longer periods than the tokenizer. Of course, when the global types change, the whole file still needs to be
 * re-scanned. But this way, we can at least limit exactly how often this happens, regardless of users behaviour.
 *
 * The type hinter is not necessarily distinct from the parser, but it should be able to extract hints even in
 * files which contain errors. The parser should be also able to recover after an error, but the recovery mechanism
 * can be, in theory, different. Also, the hinter does not really have to parse anything. In practice, we use
 * the same class, because we want the parsing errors as well as type hints.
 *
 * The biggest issue are the local function arguments, as they can shadow global definitions:
 * ```
 *      const x = 4
 *      fun plus(x,y) = x + y
 * ```
 *
 * Here, the x on the second line is clearly a local function argument. However, according to the global type hints,
 * it is a constant. We cannot solve this in the type hinter, as there is typically no way to reliably "forward"
 * the information about local types to the tokenizer (We can't use line numbers - by adding a new line in the middle
 * of a file, you completely break rest of the file until next type hinting. We can't use line hashes - by modifying
 * a line, you break all type hints on that line.). This is especially a problem if you run the tokenizer incrementally
 * only for the changed parts of the file (as most editors do).
 *
 * We solve this by including a heuristic function argument parser into the state of the tokenizer. So once the
 * tokenizer recognizes a a function declaration, it remembers all local arguments in its state until the next
 * declaration. This forces a re-scan of the function body every time the declaration changes, but that is to be
 * expected.
 *
 * Since we do not support any nested declarations, this is actually quite simple and can be done using a few custom
 * states.
 *
 * ### Enum values
 *
 * Another problematic concept are enum values (i.e. Status { OK, NOK }). They need to be distinguished by
 * the tokenizer, but cannot be inferred from type hints (OK can be a parameter while Status.OK is an enum value).
 *
 * This boils down to two problems:
 *  - Usage site classification: Once an Enum identifier and a dot is read (based on the type hints), the next
 *  identifier is an enum value.
 *  - Declaration site: Similar to function declarations, once enum declaration is detected, in the upcoming block,
 *  all identifiers are assumed to be enum values.
 *
 */
object Tokenizer {

    /**
     * Tokenizer state is used to correctly resume scanning. It has a stack structure in order to facilitate
     * the different tokenizer functions.
     *
     * The stack start with a normal state at the bottom. When encountering a string literal or a comment, an
     * appropriate state is pushed on top. Block comments can be pushed (and popped) several times. These
     * states completely override the state change logic, mainly because only a small subset of tokens is allowed
     * in these cases.
     *
     * Other pushed items act more like a "supplemental" rules, such that the rules in the normal mode still apply
     * ('//' pushes a comment, etc.), but only if the rules for the supplemental item are not met.
     *
     * Overall, there should always be just one Normal item at the bottom of the stack!
     */
    data class State(
            val stack: List<Item> = listOf(Item.Normal())
    ) {

        sealed class Item {

            /** Tokenizer has encountered a '"' and is reading a string literal. */
            object Text : Item()

            /** Tokenizer has encountered a '//' or '#' and is reading the comment value. */
            object LineComment : Item()

            /** Tokenizer has encountered a '/*' and is reading up to the next '*/'. */
            object BlockComment : Item()

            /**
             * Tokenizer is in the default state with previously inferred [localTypes].
             *  - If '#' or '//' is read, push [LineComment].
             *  - If '/*' is read, push [BlockComment].
             *  - If '"' is found, push [Text].
             *  - If 'fun' is found, push [ExpectFunName].
             *  - If 'enum' is found, push [ExpectEnumName].
             *  - If enum id is found, push [ExpectDot].
             *
             *  If any declaration is found, pop all and reset [localTypes].
             */
             */
            data class Normal(val localTypes: Set<String> = emptySet()) : Item()

            /**
             * Tokenizer has just read the 'fun' keyword and is expecting a function name.
             * If id is read, replace with [ExpectArgsOpen], otherwise pop.
             * Other rules from [Normal] apply.
             */
            object ExpectFunName : Item()

            /**
             * Tokenizer has read the 'fun' keyword and a function name and is expecting a '('.
             * If '(' is read, replace with [ExpectArgs] (empty), otherwise pop.
             * Other rules from [Normal] apply.
             */
            object ExpectArgsOpen : Item()

            /**
             * Tokenizer has read "fun name(" and is currently reading the argument list.
             * If identifier is read, add it to [localTypes].
             * If ')' is read, pop and replace [Normal] with current [localTypes].
             * Other rules from [Normal] apply.
             */
            data class ExpectArgs(val localTypes: Set<String> = emptySet()) : Item() {

                fun append(id: String) = copy(localTypes + id)

            }

            /**
             * Tokenizer has just read an identifier of an enum type and is ready to discover a dot.
             * If '.' is found, replace with [ExpectEnumValue], otherwise pop.
             * Other rules from [Normal] apply.
             */
            object ExpectDot : Item()

            /**
             * Tokenizer has just read an identifier of an enum and a dot afterwards.
             * We don't actually do anything and always pop. This is just info for the inference engine
             * that if it found and id, its an enum value.
             */
            object ExpectEnumValue : Item()

            /**
             * Tokenizer has read 'enum' and is expecting an enum name.
             * If id is read, replace with [ExpectEnumBlock], otherwise pop.
             * Other rules from [Normal] apply.
             */
            object ExpectEnumName : Item()

            /**
             * Tokenizer has read 'enum' and its name, now it expects a block start.
             * If '{' is read, replace with [ExpectEnumValues], otherwise pop.
             */
            object ExpectEnumBlock : Item()

            /**
             * Tokenizer has read "enum name {" and is reading the names of the enum values.
             * This is mostly info for the type inference engine. So we don't have to remember anything.
             * Just pop if '}' is found and apply other rules from normal mode.
             */
            object ExpectEnumValues : Item()
        }

        fun isLocalType(string: String): Boolean {
            return string in (stack[0] as Item.Normal).localTypes
        }

        /** Use this function to update a state object when a new token is scanned. */
        internal fun updateState(token: Token): State {
            val rule = token.rule
            val top = stack.last()

            // The logic seems to be too complex for a simple when statement (a lot of repetition).

            /* When in string, nothing else matters, read until the end of string or new-line. */
            if (top == Item.Text) {
                return if (rule is Literal.Text.Quote || rule is NewLine) pop() else this
            }

            /* When in line comment, read until new-line. */
            if (top == Item.LineComment) {
                return if (rule is NewLine) pop() else this
            }

            /* When in block comment, you only care about open/close tokens. Not even a new-line can stop you! */
            if (top == Item.BlockComment) {
                return when (rule) {
                    Comment.BlockOpen -> push(Item.BlockComment)
                    Comment.BlockClose -> pop()
                    else -> this
                }
            }

            return when(top) {

                Item.ExpectFunName -> when (rule) {
                    Declaration.Function -> this
                    is Identifier -> replace(Item.ExpectArgsOpen)
                    else -> replace(Item.Normal())
                }
                Item.ExpectArgsOpen -> when (rule) {
                    Declaration.Function -> replace(Item.ExpectFunName)
                    Misc.ParOpen -> replace(Item.ExpectArgs())
                    else -> replace(Item.Normal())
                }
                is Item.ExpectArgs -> when (rule) {
                    Declaration.Function -> replace(Item.ExpectFunName)
                    Misc.ParClose -> replace(Item.Normal(top.localTypes))
                    is Identifier -> replace(top.append(token.value))
                    is Declaration -> replace(Item.Normal())
                    else -> this
                }
                is Item.Normal -> when (rule) {
                    Comment.Line.StartPython, Comment.Line.StartC -> push(Item.LineComment)
                    Comment.Block.Open -> push(Item.BlockComment)
                    Literal.Text.Open -> push(Item.Text)
                    Declaration.Function -> replace(Item.ExpectFunName)
                    is Declaration -> replace(Item.Normal())
                    else -> this
                }
            }
        }

        private fun pop() = copy(stack = stack.dropLast(1))
        private fun push(item: Item) = copy(stack = stack + item)
        private fun replace(item: Item) = pop().push(item)

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