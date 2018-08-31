package biodivine.model.parser

internal inline fun <R> makeIf(condition: Boolean, constructor: () -> R): R? = if (condition) constructor() else null