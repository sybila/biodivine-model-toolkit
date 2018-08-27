package biodivine.model.parser

inline fun <R> makeIf(condition: Boolean, constructor: () -> R): R? = if (condition) constructor() else null

fun unreachable(): Nothing = error("WTF? This code should be unreachable. Definitely go and file a bug report for this.")