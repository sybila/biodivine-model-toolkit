package biodivine.model.parser

/**
 * RuleIds are strings which identify the rules outside of the Kotlin runtime (CSS rules in highlighter, etc.).
 *
 * Additionally, this object provides a useful overview of all supported token types.
 */
object RuleId {

    /** 'external': indicates that the declaration which follows it is external */
    const val EXTERNAL = "external"

    /** 'in': Used by some declarations to indicate start of the guard/range expression */
    const val IN = "in"

    /** Any whitespace that isn't a new line */
    const val WHITESPACE = "whitespace"

    /** A new line. What did you expect? */
    const val NEW_LINE = "new_line"

    /**
     * All possible types of identifiers.
     * Note that tokenizer cannot always infer these correctly, so they are just for visualisation! Do not use them
     * in the parser.
     */
    object Identifier {

        /** Default identifier. We don't know the type */
        const val UNSPECIFIED = "id_unspecified"

        /** Undefined references w*/
        const val MISSING = "id_unknown"

        /** A constant reference */
        const val CONSTANT = "id_constant"

        /** A function call */
        const val FUNCTION = "id_function"

        /** A variable reference */
        const val VARIABLE = "id_variable"

        /** A parameter reference */
        const val PARAMETER = "id_parameter"

        /** A function argument */
        const val ARGUMENT = "id_argument"

        /** An Enum reference */
        const val ENUM = "id_enum"

        /** A value of an Enum */
        const val ENUM_VALUE = "id_enum_value"

        /** An annotation name. Always starts with '@' */
        const val ANNOTATION = "id_annotation"

        /** An external constant */
        const val E_CONSTANT = "id_constant_ext"

        /** An external function */
        const val E_FUNCTION = "id_function_ext"

    }

    /** Numeric, boolean or text literal. */
    object Literal {

        /** Any decimal number, possibly with scientific notation */
        const val NUMBER = "literal_number"

        /** 'true' */
        const val TRUE = "literal_true"

        /** 'false' */
        const val FALSE = "literal_false"

        /** Anything that goes into a string */
        object Text {

            /** '"' either opens or closes a string, depending on the tokenizer state */
            const val QUOTE = "str_open"

            /** A pair of chars where first is \ and the second is the escaped character, for example '\"' */
            const val ESCAPE_CHAR = "str_escape"

            /** Anything else that is inside the string and isn't escaped */
            const val VALUE = "str_value"

        }
    }

    /** All block and line comments */
    object Comment {

        /** '/*' Starts a (possibly nested) block comment. */*/
        const val BLOCK_OPEN = "comment_block_open"

        /** /*'*/' Ends a (possibly nested) block comment. */
        const val BLOCK_CLOSE = "comment_block_close"

        /** '//' Start a C-like line comment */
        const val START_C = "comment_line_c"

        /** '#' Start a Python-like line comment */
        const val START_PYTHON = "comment_line_python"

        // We differentiate the two value types because they are actually scanned differently!

        /** Everything else that's inside a comment */
        const val BLOCK_VALUE = "comment_block_value"

        /** Everything else that's inside a comment */
        const val LINE_VALUE = "comment_line_value"

    }

    /** All unary and binary logical, comparison and numeric operators */
    object Operator {

        /** '&&' logical and */
        const val AND = "operator_and"

        /** '||' logical or */
        const val OR = "operator_or"

        /** '!' logical not */
        const val NOT = "operator_not"

        /** '==' equal to */
        const val EQ = "operator_eq"

        /** '!=' not eal to */
        const val NEQ = "operator_neq"

        /** '>' greater than */
        const val GT = "operator_gt"

        /** '>=' greater or equal than */
        const val GE = "operator_ge"

        /** '<' less than */
        const val LT = "operator_lt"

        /** '<=' less or equal than */
        const val LE = "operator_le"

        /** '+' numeric addition */
        const val PLUS = "operator_plus"

        /** '-' numeric subtraction */
        const val MINUS = "operator_minus"

        /** '/' numeric division */
        const val DIV = "operator_div"

        /** '*' numeric multiplication */
        const val MUL = "operator_mul"

    }

    /** Other non-alphanumeric tokens, meaning can depend on context */
    object Misc {

        /** '(' */
        const val PAR_OPEN = "misc_parenthesis_open"

        /** ')' */
        const val PAR_CLOSE = "misc_parenthesis_close"

        /** '{' */
        const val BLOCK_OPEN = "misc_block_open"

        /** '}' */
        const val BLOCK_CLOSE = "misc_block_close"

        /** '[' */
        const val BRACKET_OPEN = "misc_bracket_open"

        /** ']' */
        const val BRACKET_CLOSE = "misc_bracket_close"

        /** ',' */
        const val COMMA = "misc_comma"

        /** '=' */
        const val ASSIGN = "misc_assign"

        /** '..' */
        const val RANGE = "misc_range"

        /** '->' */
        const val THEN = "misc_then"

        /** '.' */
        const val DOT = "misc_dot"

        /** '|' */
        const val CASE = "misc_case"

    }

    /** Keywords which start a new declarations */
    object Declaration {

        /** 'const' Constant */
        const val CONST = "keyword_const"

        /** 'fun' Function */
        const val FUN = "keyword_fun"

        /** 'enum' Enum */
        const val ENUM = "keyword_enum"

        /** 'param' Parameter */
        const val PARAM = "keyword_param"

        /** 'event' Event */
        const val EVENT = "keyword_event"

        /** 'var' Variable */
        const val VAR = "keyword_var"

    }

}