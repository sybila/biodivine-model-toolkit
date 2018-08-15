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
 */
data class ModelFile(
        val name: String,
        val declarations: Set<Declaration>
) {

    data class Annotation(val name: String)

    sealed class Declaration {



    }
}