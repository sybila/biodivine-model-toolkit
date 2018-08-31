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

        /* Comments and Strings */

            ${rules(
                Comment.StartC, Comment.StartPython, Comment.LineValue,
                Comment.BlockOpen, Comment.BlockClose, Comment.BlockValue
            )} {
                color: ${Colors.commentGray};
            }

            ${rules(Literal.Text.Quote, Literal.Text.Value)} {
                color: ${Colors.stringGreen};
            }

            ${rules(Literal.Text.EscapeChar)} {
                color: ${Colors.keywordOrange};
            }

        /* Literals and special chars */

            ${rules(Literal.Number)} {
                color: ${Colors.numberBlue};
            }

            ${rules(Literal.True, Literal.False, Misc.Comma)} {
                color: ${Colors.keywordOrange}
            }

        /* Keywords */

            ${rules(
                Declaration.Constant, Declaration.Function, Declaration.Variable,
                Declaration.Parameter, Declaration.Enum, Declaration.Event, In, External
            )} {
                color: ${Colors.keywordOrange};
            }

        /* Type-hinted identifiers */

            ${rules(Identifier.Unknown)} {
                color: ${Colors.unknownRed};
            }

            ${rules(Identifier.Annotation)} {
                color: ${Colors.annotationYellow};
            }

            ${rules(Identifier.EnumValue)} {
                color: ${Colors.numberBlue};
            }

            ${rules(Identifier.Function)} {
                color: ${Colors.globalViolet};
            }

            ${rules(Identifier.Parameter)} {
                color: ${Colors.parameterTeal};
            }

            ${rules(Identifier.Constant)} {
                color: ${Colors.localYellow};
                font-style: italic;
            }

            ${rules(Identifier.Variable)} {
                color: ${Colors.localYellow};
            }

        /* Unknown tokens */

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