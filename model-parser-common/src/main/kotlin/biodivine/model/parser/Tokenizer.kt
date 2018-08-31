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
class Tokenizer(
        var typeHints: Map<String, Identifier>
) {

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

            object Text : Item()

            object LineComment : Item()

            object BlockComment : Item()

            data class Normal(val arguments: Set<String> = emptySet()) : Item()

            object ExpectFunName : Item()

            object ExpectFunArgsStart : Item()

            data class ScanningFunArgs(val arguments: Set<String> = emptySet()) : Item() {

                fun append(id: String) = copy(arguments + id)

            }

            object ExpectDot : Item()

            object ExpectEnumValue : Item()

            object ExpectEnumName : Item()

            object ExpectEnumBlockStart : Item()

            object ScanningEnumValues : Item()
        }

        fun isArgument(string: String): Boolean {
            return string in (stack[0] as Item.Normal).arguments || stack.last() is Item.ScanningFunArgs
        }

        /** Use this function to update a state object when a new token is scanned. */
        internal fun updateState(token: Token): State {
            val rule = token.rule
            val top = stack.last()

            // The logic seems to be too complex for a simple when statement (a lot of repetition).

            /* When in string, nothing else matters, read until the end of string or new-line. */
            if (top === Item.Text) {
                return if (rule is Literal.Text.Quote || rule is NewLine) pop() else this
            }

            /* When in line comment, read until new-line. */
            if (top === Item.LineComment) {
                return if (rule is NewLine) pop() else this
            }

            /* When in block comment, you only care about open/close tokens. Not even a new-line can stop you! */
            if (top === Item.BlockComment) {
                return when (rule) {
                    Comment.BlockOpen -> push(Item.BlockComment)
                    Comment.BlockClose -> pop()
                    else -> this
                }
            }

            // Now, if you encounter a comment, you jump right into that mode, regardless
            // of what other stuff you are scanning right now, because comments do not have meaning.
            // (String can pop you out of other modes, so we will deal with them later)

            if (rule === Comment.BlockOpen) {
                return push(Item.BlockComment)
            }

            if (rule === Comment.StartC || rule === Comment.StartPython) {
                return push(Item.LineComment)
            }

            // Now we have the comments handled and we are not inside a string (we might be starting one though).

            /*
                We are starting a string. If we are in some of the "expect" rules, we drop them, otherwise
                continue what you were doing.
             */
            if (rule === Literal.Text.Quote) {
                return when(top) {
                    Item.ScanningEnumValues, is Item.Normal, is Item.ScanningFunArgs -> push(Item.Text)
                    else -> pop().push(Item.Text)   // expectXXX rules -> expected token not found, popping and going to string.
                }
            }

            /* If the rule is a declaration, we have to reset everything. */
            if (rule is Declaration) {
                val blank = State() // no local variables
                return when (rule) {
                    Declaration.Function -> blank.push(Item.ExpectFunName)
                    Declaration.Enum -> blank.push(Item.ExpectEnumName)
                    else -> blank
                }
            }

            // Helper function which replaces the top with the given item if test is satisfied on
            // the next non-Whitespace token.
            inline fun replaceWhen(toReplace: Item, test: () -> Boolean): State {
                return if (test()) replace(toReplace) else if (rule === Whitespace || rule === NewLine) this else pop()
            }

            // Here, we handle function declarations for extracting argument names:

            /* If we are expecting a function name and we get it, move to a next state, otherwise abort. */
            if (top === Item.ExpectFunName) {
                return replaceWhen(Item.ExpectFunArgsStart) { rule is Identifier }
            }

            /* Same for '(' */
            if (top === Item.ExpectFunArgsStart) {
                return replaceWhen(Item.ScanningFunArgs()) { rule === Misc.ParOpen }
            }

            /* We are reading function args until the next ')' */
            if (top is Item.ScanningFunArgs) {
                return when(rule) {
                    is Identifier -> replace(top.append(token.value))
                    Misc.ParClose -> State(listOf(Item.Normal(top.arguments)))
                    else -> this
                }
            }

            // Here, we handle enum declarations (we don't extract anything, we just provide info for type hinter.

            if (top === Item.ExpectEnumName) {
                return replaceWhen(Item.ExpectEnumBlockStart) { rule is Identifier }
            }

            if (top === Item.ExpectEnumBlockStart) {
                return replaceWhen(Item.ScanningEnumValues) { rule === Misc.BlockOpen }
            }

            if (top === Item.ScanningEnumValues) {
                return when(rule) {
                    Misc.BlockClose -> pop()
                    else -> this
                }
            }

            // Finally, handle enum values outside of declarations (relies on type hinting).
            if (rule === Identifier.Enum && top is Item.Normal) {
                return push(Item.ExpectDot)
            }

            if (top === Item.ExpectDot) {
                return replaceWhen(Item.ExpectEnumValue) { rule === Misc.Dot }
            }

            if (top === Item.ExpectEnumValue) {
                return if (rule === Whitespace || rule === NewLine) this else pop()
            }

            // If nothing else happened, the state did not change.
            return this
        }

        private fun pop() = copy(stack = stack.dropLast(1))
        private fun push(item: Item) = copy(stack = stack + item)
        private fun replace(item: Item) = pop().push(item)

        val shouldHandleIdsAsEnumValues: Boolean
            get() = stack.last().let { it === Item.ExpectEnumValue || it === Item.ScanningEnumValues }

        val top: Item
            get() = stack.last()

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
        return result to if (result.lastOrNull()?.rule === NewLine) currentState else {
            currentState.updateState(Token(NewLine, "\n", -1))
        }
    }

    private val scanRules = listOf(
            Whitespace, NewLine,                                        // \s \n
            Comment.BlockOpen, Comment.BlockClose,                      // /* */
            Comment.StartC, Comment.StartPython,                        // // #
            Misc.ParOpen, Misc.ParClose,                                // ( )
            Misc.BlockOpen, Misc.BlockClose,                            // { }
            Misc.BracketOpen, Misc.BracketClose,                        // [ ]
            Misc.Range, Misc.Dot, Misc.Comma, Misc.Then,                // .. . , ->
            Operator.GreaterEqual, Operator.Greater,                    // >= >
            Operator.LessEqual, Operator.Less,                          // <= <
            Operator.NotEqual, Operator.Equal,                          // != ==
            Operator.And, Operator.Or, Operator.Not,                    // && || !
            Operator.Plus, Operator.Minus, Operator.Div, Operator.Mul,  // + - / *
            Misc.Case, Misc.Assign,                                     // | =
            Literal.Text.Quote, Literal.Number,                         // " 123
            Identifier.Unspecified                                      // abc123
    )

    private val keywords: List<ExactRule> = listOf(
            Literal.True, Literal.False, In, External, Declaration.Enum, Declaration.Function,
            Declaration.Constant, Declaration.Event, Declaration.Parameter, Declaration.Variable
    )

    fun scanToken(line: String, position: Int, state: State): Pair<Token, State> {
        if (position < 0 || position >= line.length) {
            error("Invalid position $position in line of length ${line.length}")
        }
        val token = when (state.top) {
            State.Item.LineComment -> (Comment.LineValue.scanToken(line, position) ?: unreachable())
            is State.Item.BlockComment -> {
                Comment.BlockOpen.scanToken(line, position) ?:
                Comment.BlockClose.scanToken(line, position) ?:
                Comment.BlockValue.scanToken(line, position) ?:
                unreachable()
            }
            State.Item.Text -> {
                Literal.Text.Quote.scanToken(line, position) ?:
                Literal.Text.EscapeChar.scanToken(line, position) ?:
                Literal.Text.Value.scanToken(line, position) ?:
                unreachable()
            }
            else -> {
                // All other states can recognize all tokens, there are just differences on identifier type hinting.
                val token = scanRules.asSequence()
                        .map { it.scanToken(line, position) }
                        .filterNotNull().firstOrNull()

                when {
                    // unknown token
                    token == null -> Token(null, line.substring(position, position + 1), position)
                    token.rule !is Identifier -> token
                    else -> {
                        // First, handle keywords:
                        keywords.find { it.value == token.value }?.let { keyword ->
                            token.copy(rule = keyword)
                        }
                        // If the token is not a keyword, handle type hints:
                        ?: when {
                            token.value.startsWith('@') -> token.copy(rule = Identifier.Annotation)
                            // If this is "enum Foo { A, B }" or "Foo.A", mark A, B as enum values.
                            state.shouldHandleIdsAsEnumValues -> token.copy(rule = Identifier.EnumValue)
                            // If this is after "fun foo(x, y)", mark x y as function arguments.
                            state.isArgument(token.value) -> token.copy(rule = Identifier.Argument)
                            // Otherwise, use type hints.
                            else -> token.copy(rule = typeHints[token.value] ?: Identifier.Unspecified)
                        }
                    }
                }
            }
        }

        return (token to state.updateState(token)).also { (a, b) ->
            println("Scanned $a from $state, new state $b")
        }
    }

}