package biodivine.model.parser

/**
 * RuleIds are strings which identify the rules outside of the Kotlin runtime (CSS rules in highlighter, etc.).
 */
object RuleId {

    object Identifier {
        const val UNSPECIFIED = "id_unspecified"
        const val UNKNOWN = "id_unknown"
        const val CONSTANT = "id_constant"
        const val FUNCTION = "id_function"
        const val VARIABLE = "id_variable"
        const val PARAMETER = "id_parameter"
        const val E_CONSTANT = "id_constant_ext"
        const val E_FUNCTION = "id_function_ext"
        const val E_VARIABLE = "id_variable_ext"
        const val E_PARAMETER = "id_parameter_ext"
    }

    object Literal {
        const val NUMBER = "literal_number"
        const val TRUE = "literal_true"
        const val FALSE = "literal_false"
        object Text {
            const val OPEN = "str_open"
            const val CLOSE = "str_close"
            const val ESCAPE_CHAR = "str_escape"
            const val VALUE = "str_value"
        }
    }

    object Comment {
        object Block {
            const val OPEN = "comment_block_open"
            const val CLOSE = "comment_block_close"
            const val VALUE = "comment_block_value"
        }
        object Line {
            const val START_C = "comment_line_c"
            const val START_PYTHON = "comment_line_python"
            const val VALUE = "comment_line_value"
        }
    }

    object Operator {
        const val AND = "operator_and"
        const val OR = "operator_or"
        const val NOT = "operator_not"
        const val PLUS = "operator_plus"
        const val MINUS = "operator_minus"
        const val DIV = "operator_div"
        const val MUL = "operator_mul"
        const val GT = "operator_gt"
        const val GE = "operator_ge"
        const val LT = "operator_lt"
        const val LE = "operator_le"
        const val EQ = "operator_eq"
        const val NEQ = "operator_neq"
    }

    object Misc {
        const val PAR_OPEN = "misc_parenthesis_open"
        const val PAR_CLOSE = "misc_parenthesis_close"
        const val BLOCK_OPEN = "misc_block_open"
        const val BLOCK_CLOSE = "misc_block_close"
        const val BRACKET_OPEN = "misc_bracket_open"
        const val BRACKET_CLOSE = "misc_bracket_close"
        const val COMMA = "misc_comma"
        const val WHITESPACE = "misc_whitespace"
        const val ASSIGN = "misc_assign"
        const val RANGE = "misc_range"
        const val THEN = "misc_then"
    }

    object Keyword {
        const val EXTERNAL = "keyword_external"
        const val CONST = "keyword_const"
        const val FUN = "keyword_fun"
        const val WHEN = "keyword_when"
    }

}