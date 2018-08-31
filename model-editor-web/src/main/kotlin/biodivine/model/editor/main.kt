package biodivine.model.editor

import ace.Ace

fun main(args: Array<String>) {

    val editor = Ace.edit("editor")
    editor.setTheme(DarkTheme.id)
    editor.getSession().setMode(AceMode)

}

/**
 * A JS interface of the serialised Kotlin pair type. This is all that remains after serialisation.
 */
external interface EPair<A, B> {
    val first: A
    val second: B
}

operator fun <A> EPair<A, *>.component1(): A = this.first
operator fun <B> EPair<*, B>.component2(): B = this.second