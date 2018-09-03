package biodivine.model.parser

interface AST {
    val tokens: List<Token>
}

data class ParseError(override val message: String, override val tokens: List<Token>) : AST, Throwable()

data class NumericLiteral(val value: String, override val tokens: List<Token>) : AST

data class StringLiteral(val value: String, override val tokens: List<Token>) : AST

sealed class BoolLiteral(override val tokens: List<Token>) : AST {
    class TrueLiteral(tokens: List<Token>) : BoolLiteral(tokens)
    class FalseLiteral(tokens: List<Token>) : BoolLiteral(tokens)
}

data class Reference(val value: String, override val tokens: List<Token>) : AST

data class FunctionCall(
        val name: String, val arguments: List<AST>, override val tokens: List<Token>
) : AST