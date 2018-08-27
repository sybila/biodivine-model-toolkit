package biodivine.model.parser.token

import biodivine.model.parser.*

sealed class Comment(override val id: String) : Rule {

    sealed class Block(id: String) : Comment(id) {

        object Open : Block(RuleId.Comment.Block.OPEN), ExactRule {
            override val value: String = "/*"
        }

        object Close : Block(RuleId.Comment.Block.CLOSE), ExactRule {
            override val value: String = "*/"
        }

        object Value : Block(RuleId.Comment.Block.VALUE) {

            override fun scanToken(line: String, position: Int): Token? =
                    line.scanWhile(position) { i, _ ->
                        Open.scanToken(line, i) == null && Close.scanToken(line, i) == null
                    }?.toToken(position)

        }

    }

    sealed class Line(id: String) : Comment(id) {

        object StartC : Line(RuleId.Comment.Line.START_C), ExactRule {
            override val value: String = "//"
        }

        object StartPython : Line(RuleId.Comment.Line.START_PYTHON), ExactRule {
            override val value: String = "#"
        }

        object Value : Line(RuleId.Comment.Line.VALUE) {

            override fun scanToken(line: String, position: Int): Token? =
                    line.substring(position).takeIf { it.isNotEmpty() }?.toToken(position)

        }

    }

}