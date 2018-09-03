package biodivine.model.parser

internal inline fun <R> makeIf(condition: Boolean, constructor: () -> R): R? = if (condition) constructor() else null

/** Target character must be preceded with \ **/
fun Char.unescapeChar(): Char = when (this) {
    't' -> '\t'
    'b' -> '\b'
    'n' -> '\n'
    'r' -> '\r'
    else -> this
}