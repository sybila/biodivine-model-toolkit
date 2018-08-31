package biodivine.model.parser

import biodivine.model.parser.token.*

/**
 * Type hinter extracts type information from a model tokens, ignoring almost all structural information.
 *
 * It essentially just looks for "declaration name" pairs and then assigns type according to declaration.
 * It also identifies unknown names as anything that cannot be typed according to this simple rule.
 *
 * Technically, this is incorrect (it will mark all function arguments as unknown, etc.). But it is only
 * incorrect in situations where type hints are overridden by local info in the tokenizer state, i.e.
 * for function arguments and enum values.
 *
 * It can also behave weirdly for incorrect files (double declarations, messed up structure...), but
 * in this case, there is not "right" solution.
 */
object TypeHinter {

    /**
     * Take all [modelTokens] and extract type information from it.
     */
    fun extractHints(modelTokens: List<Token>): Map<String, Identifier> {
        val tokens = modelTokens.filter { it.rule !is Comment && it.rule !== Whitespace && it.rule !== NewLine }

        // First, identify types.
        val typeHints = HashMap<String, Identifier>()
        for (i in tokens.indices) {
            val token = tokens[i]
            if (token.rule is Declaration) {
                val nextToken = tokens.getOrNull(i + 1)
                if (nextToken?.rule is Identifier) {
                    typeHints[nextToken.value] = when (token.rule) {
                        Declaration.Enum -> Identifier.Enum
                        Declaration.Function -> Identifier.Function
                        Declaration.Parameter -> Identifier.Parameter
                        Declaration.Variable -> Identifier.Variable
                        Declaration.Constant -> Identifier.Constant
                        else -> Identifier.Unspecified
                    }
                }
            }
        }

        // Second, identify unknown IDs
        for (token in tokens) {
            if (token.rule is Identifier && token.value !in typeHints) {
                typeHints[token.value] = Identifier.Unknown
            }
        }

        return typeHints.filterValues { it !== Identifier.Unspecified }
    }

}