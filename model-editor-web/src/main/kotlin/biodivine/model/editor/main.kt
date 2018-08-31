package biodivine.model.editor

import ace.Ace

fun main(args: Array<String>) {

    val editor = Ace.edit("editor")
    editor.setTheme(DarkTheme.id)
    editor.getSession().setMode(AceMode)
}