package biodivine.model.parser

import biodivine.model.parser.token.*

/**
 * Parser accepts a full list of tokens for a model. It then proceeds to translate this list into an abstract
 * syntax tree (AST). AST is an intermediate representation between tokens and model file. It allows precise
 * localisation and description of errors and incomplete models (Model file can be created only from correctly
 * parsed models). However, remember that model file is still not completely verified (missing references, type
 * mismatch, etc.).
 *
 * The parser proceeds by recursively trying to parse different rules of the model grammar depending on the current
 * state. Each rule parsing can fail and return a null. Upon such result, it is the job of the caller
 * to recover from this error (try another rule or try to recover, emitting an appropriate AST error).
 *
 * Note that the token list still contains all comments and whitespace. This is in order to be able to
 * recover all info about the position in the file from the [AST.tokens] value.
 */
object Parser {

    private const val ERROR_UNCLOSED_STRING = "String literal needs to be terminated with \""
    private const val ERROR_UNCLOSED_LIST = "Expecting closing token"   // general error you should probably replace
    private const val ERROR_EXPECTING_EXPRESSION = "Expecting expression"
    private const val ERROR_EXTRA_FUNCTION_ARGS = "Expecting ')' or ','"
    private const val ERROR_UNCLOSED_FUNCTION_CALL = "Expecting ')' at the end of function call"

    /** Read an arbitrary expression which starts (exactly) at given [position]. */
    private fun List<Token>.readExpression(position: Int): AST? {
        TODO()
    }

    /** Read a numeric literal which is (exactly) at [position]. If no numeric literal is present, return null. */
    private fun List<Token>.readNumericLiteral(position: Int): AST? {
        val token = getOrNull(position)
        return if (token?.rule is Literal.Number) {
            NumericLiteral(token.value, oneToken(position))
        } else null
    }

    /** Read a boolean literal which is (exactly) at [position]. If no boolean literal is present, return null. */
    private fun List<Token>.readBoolLiteral(position: Int): AST? {
        val token = getOrNull(position)
        return when (token?.rule) {
            Literal.True -> BoolLiteral.TrueLiteral(oneToken(position))
            Literal.False -> BoolLiteral.FalseLiteral(oneToken(position))
            else -> null
        }
    }

    /** Read a reference expression which is (exactly) at [position]. In reference is not present, return null. */
    private fun List<Token>.readReference(position: Int): AST? {
        val token = getOrNull(position)
        return if (token?.rule is Identifier) {
            Reference(token.value, oneToken(position))
        } else null
    }

    /**
     * Read a string literal which starts (exactly) at [position]. If string literal does not start there, return null.
     * If it starts there, but is invalid (unclosed), return [ParseError].
     */
    private fun List<Token>.readStringLiteral(position: Int): AST? {
        val token = getOrNull(position)
        if (token?.rule !is Literal.Text.Quote) return null // We did not find ", there is no string here.

        val result = StringBuilder()
        var i = position+1
        scan@while (true) {
            val innerToken = getOrNull(i)
            when (innerToken?.rule) {
                // If EOF, wrap the rest of file into error node.
                null -> return ParseError(ERROR_UNCLOSED_STRING, subList(position, size))
                // IF EOL, wrap the rest of the string into error node.
                NewLine -> return ParseError(ERROR_UNCLOSED_STRING, subList(position, i))
                Literal.Text.Value -> result.append(innerToken.value)
                Literal.Text.EscapeChar -> result.append(innerToken.value[1].unescapeChar())
                Literal.Text.Quote -> break@scan
                // Technically, this should not happen unless the tokenizer is broken, but we can't really rely on that.
                else -> return ParseError(ERROR_UNCLOSED_STRING, subList(position, i))
            }
            i += 1
        }
        // If we got here, this[i] is a quote (everything else goes into error).
        return StringLiteral(result.toString(), subList(position, i+1))
    }

    /**
     * Read a function call which starts exactly at [position]. If there is no 'id(' there, return null.
     * Otherwise read until next ')'.
     */
    private fun List<Token>.readFunctionCall(position: Int): AST? {
        val idToken = getOrNull(position)   // check presence of 'id'
        if (idToken?.rule !is Identifier) return null
        val argsOpenPosition = nextTokenIndex(position) // check presence of 'id('
        val argsOpenToken = getOrNull(argsOpenPosition)
        if (argsOpenToken?.rule !is Misc.ParOpen) return null

        val arguments = readExpressionList(
                position = nextTokenIndex(argsOpenPosition + 1),
                expectingExpressionError = ERROR_EXPECTING_EXPRESSION,
                unclosedListError =
        )
        return FunctionCall(idToken.value, arguments, subList(position, close+1))
    }

    /**
     * Read a list of items which starts (exactly) at given [position].
     * The list starts with the [start] token, followed by zero or more expressions, each separated
     * by the [separator] token, ended with the [stop] token. Each item is read using the [readItem]
     * function.
     *
     * When the start token is not found, the returned list is null.
     * If the stop token is not found, a [ParseError] is thrown to indicate unclosed expression list.
     * If arguments are missing (premature separator token - [missingItemError]) or unexpected
     * (expression not followed by separator or stop - [trailingTokensError]), add it to the list as [ParseError].
     */
    private fun List<Token>.readItemList(position: Int,
                                         start: Rule = Misc.ParOpen,
                                         separator: Rule = Misc.Comma,
                                         stop: Rule = Misc.ParClose,
                                         missingItemError: String,
                                         trailingTokensError: String,
                                         readItem: List<Token>.(Int) -> AST?
    ): List<AST>? {
        val openToken = getOrNull(position)
        if (openToken?.rule != start) return null
        val items = ArrayList<AST>()

        // Check if the list isn't empty.
        val firstToken = nextTokenIndex(position + 1)
        if (getOrNull(firstToken)?.rule == stop) return items

        // next always points to the position of the next expected expression
        var next = firstToken   // firstToken isn't stop, so it should be an expression
        while (true) {
            val item = readItem(next) ?: ParseError(missingItemError, emptyList())
            items.add(item)
            // Next token after expression. Should be separator or stop.
            next = nextTokenIndex(next + item.tokens.size)
            // If its not a separator, the list ends (but can still have extra args error).
            if (getOrNull(next)?.rule != separator) break
            next = nextTokenIndex(next + 1) // skip the separator (1 token)
        }

        // Now we have read the valid list with possible empty expressions. We may still have some trailing
        // tokens, so we have to find the end of the list.

        // increase close until it is stop or the parser can recover on another declaration/annotation
        var close = next
        while (close < size && this[close].rule.isNotReset() && this[close].rule != stop) {
            close = nextTokenIndex(close + 1)
        }
        if (close == size || this[close].rule.isReset()) {
            // Unclosed list
            throw ParseError(ERROR_UNCLOSED_LIST, subList(position, close))
        }
        if (close != next) {
            // There are some extra tokens after last item
            items.add(ParseError(trailingTokensError, subList(next, close)))
        }
        // close is stop here and list is complete (other conditions go to error)
        return items
    }

    private fun Rule?.isNotReset(): Boolean = !isReset()
    private fun Rule?.isReset(): Boolean = this is Declaration || this is Identifier.Annotation

    /**
     * Returns position of the next significant token. If the token stream ends, returns [this.size].
     */
    private fun List<Token>.nextTokenIndex(position: Int): Int {
        var i = position
        while (i < size) {
            val rule = this[i].rule
            if (rule !is Whitespace && rule !is NewLine && rule !is Comment) return i
            i += 1
        }
        // End of tokens
        return size
    }

    /** Extract one token sublist from this list. */
    private fun List<Token>.oneToken(at: Int) = subList(at, at + 1)


}