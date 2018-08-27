package biodivine.model.editor

import ace.theme.Theme
import ace.theme.register
import biodivine.model.parser.Rule
import biodivine.model.parser.token.*

object DarkTheme : Theme {

    private const val name = "editor-dark"

    override val cssClass: String = name
    override val id: String = "ace/theme/$name"
    override val isDark: Boolean = true

    override val cssString: String = """

        /* Basic styling of editor */

        .$name {
            color: #A9B7C6;
            background: #2B2B2B;
        }

        .$name .ace_gutter {
            color: #606366;
            background: #313335;
        }

        .$name .ace_cursor {
            color: #ababab;
        }

        .$name .ace_marker-layer .ace_selection {
            background: rgba(221, 240, 255, 0.20);
        }

        .$name .ace_marker-layer .ace_active-line {
            background: rgba(255, 255, 255, 0.031);
        }

        .$name .ace_gutter-active-line {
            rgba(255, 255, 255, 0.031);
        }

        /* Token styling (based on IntelliJ IDEA) */

        ${rules(
            Comment.Line.StartC, Comment.Line.StartPython, Comment.Line.Value,
            Comment.Block.Open, Comment.Block.Close, Comment.Block.Value
        )} {
            color: ${Colors.commentGray};
        }

        ${rules(Literal.Text.Open, Literal.Text.Close, Literal.Text.Value)} {
            color: ${Colors.stringGreen};
        }

        ${rules(Literal.Text.EscapeChar)} {
            color: ${Colors.keywordOrange};
        }

        ${rules(Literal.Number)} {
            color: ${Colors.numberBlue};
        }

        ${rules(Literal.True, Literal.False)} {
            color: ${Colors.keywordOrange}
        }

        ${rules(
            Keyword.Const, Keyword.Function, Keyword.When, Keyword.Var,
            Keyword.Param, Keyword.Enum, Keyword.In, Keyword.Event, Keyword.External
        )} {
            color: ${Colors.keywordOrange};
        }

        ${rules(Identifier.Unknown)} {
            color: ${Colors.unknownRed};
        }

        ${rules(Identifier.Annotation)} {
            color: ${Colors.annotationYellow};
        }

        .$name .ace_unknown {
            -webkit-text-decoration-line: underline;
            -webkit-text-decoration-color: red;
            text-decoration-line: underline;
            text-decoration-color: red;
        }

    """.trimIndent()


    init {
        register()
    }

    /* foo */
    private fun rules(vararg rules: Rule) = rules.joinToString(separator = ", ") { ".$name .ace_${it.id}" }

    object Colors {
        const val keywordOrange = "#CC7832"
        const val stringGreen = "#6A8759"
        const val numberBlue = "#6897BB"
        const val globalViolet = "#FFC66D"
        const val localYellow = "#9876AA"
        const val parameterTeal = "#20999D"
        const val textGray = "#A9B7C6"
        const val annotationYellow = "#BBB529"
        const val commentGray = "#808080"
        const val unknownRed = "#BC3F3C"

        @Suppress("unused") // used in css for backgrounds, etc.
        const val bg700 = "#3C3F41"
        const val bg500 = "#313335"
        const val bg300 = "#2B2B2B"

        @Suppress("unused") // used in css
        const val separator = "#4B4B4B"
    }

}