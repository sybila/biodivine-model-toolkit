package biodivine.model.parser

/**
 * Model file contains all data extracted from a single `.model` file, including all metadata necessary
 * to further post-process the model (line numbers, annotations). The format does not contain information
 * about whitespace, indentation or parenthesis. It is also not guaranteed to be sane (it can contain
 * undefined references, be incorrectly typed), but it is guaranteed to be parsed correctly (no
 * undefined tokens, mismatched parenthesis).
 *
 * The file variables, parameters and constants can be declared using different types:
 *  - `Int` signed, unbounded integer
 *  - `Real` real number
 *  - `Bool` true/false
 *  - Custom enum type
 * Furthermore, in annotations, one can also use standard strings and mixed arrays. These are not supported in the
 * model language yet though.
 *
 * Model file consist of a set of (intentionally unordered) declarations. Each declaration can span
 * multiple lines and can define:
 *  - A custom enum type:
 *
 * `enum State { OK, NOK }`
 *
 *  - A typed constant value (the type can be inferred based on the type of the value):
 *
 * ```
 * const Pi: Real = 3.14
 * const num_components = 4
 * const isAllowed = false
 * const initialState = State.OK
 * ```
 *
 *  - A typed parameter or variable declaration with optional range:
 *
 * ```
 * param k: Real in [4.0 .. 6.7]
 * var x: Int in [-3 .. 10e4]
 * var isActive: Bool
 * param initialValue: MyEnumType
 * ```
 *
 *  - A function declaration:
 *
 * ```
 * fun kineticRate(x: Real, i: Int): Real = expression...
 * ```
 *
 *  - An external reference:
 *
 *  ```
 *  external const Pi: Real
 *  external fun sin(Real): Real
 *  external var time: Real
 *  ```
 *
 *  Informal grammar (missing stuff like associativity, priority, parenthesis, etc.):
 *
 *  file: declaration*
 *  declaration: annotation* definition
 *
 *  annotation: '@'ID ( '(' annotation_arguments? ')' )?
 *  annotation_arguments: ID '=' expression (',' ID '=' expression)*
 *
 *  definition: external_definition | constant | parameter | variable | flow | enum | fun
 *
 *  constant: 'const' ID ':' ID '=' expression
 *  parameter: 'param' ID ':' ID 'in' bounds
 *  variable: 'var' ID ':' ID 'in' bounds
 *  flow: 'flow' ID '=' expression
 *  enum: 'enum' ID '{' ID (, ID)* '}'
 *  fun: 'fun' ID '(' function_arguments? ')' ':' ID '=' expression
 *
 *  external_definition: e_constant | e_parameter | e_variable | e_fun
 *  e_constant: 'const' ID ':' ID
 *  e_parameter: 'param' ID ':' ID
 *  e_variable: 'var' ID ':' ID
 *  e_fun: 'fun' ID '(' function_arguments? ')' ':' ID
 *
 *  function_arguments: ID ':' ID (',' ID ':' ID)*
 *
 *
 *  identifier: [a-zA-Z][a-zA-Z0-9_]
 *
 *  // Arithmetic expressions:
 *  // Arithmetic expressions cannot implicitly cast between real numbers and integers
 *  // negative numbers are handled using the unary minus operator which is then directly re-inlined when possible.
 *  integer: \d+
 *  real: \d+.\d+([eE](-)?\d+)?
 *  a_expression: minus_expression
 *  minus_expression: plus_expression ('-' minus_expression)?
 *  plus_expression: div_expression ('+' plus_expression)?
 *  div_expression: mul_expression ('/' div_expression)?
 *  mul_expression: unary_expression ('*' mul_expression)?
 *  unary_expression: ('+'|'-') unary_expression | a_atom
 *  a_switch: 'switch' '{' b_expression ':' a_expression '}'
 *
 *  a_atom: integer | real | identifier | '(' a_expression ')' | a_switch
 *
 *  // Boolean expression:
 *  b_expression: or_expression
 *  or_expression: and_expression ('||' or_expression)?
 *  and_expression: not_expression ('&&' and_expression)?
 *  not_expression: '!' not_expression | b_atom
 *  b_switch: 'switch' '{' b_expression ':' b_expression '}'
 *
 *  b_atom: 'true' | 'false' | identifier | '(' b_expression ')' | b_switch | a_expression cmp a_expression
 *
 *  // Arrays:
 *  // array is a list of arbitrary expressions - we currently don't support any operations on arrays, they are
 *  // mostly for annotating with lists of values/constraints/etc.
 *  array: '[' array_values? ']'
 *  array_values: expression (',' expression)*
 *
 *
 *  expression: a_expression | b_expression | array | string
 *
 */
data class ModelFile(
        val name: String,
        val declarations: Set<Declaration>
) {

    data class Annotation(val name: String)

    sealed class Declaration {



    }
}