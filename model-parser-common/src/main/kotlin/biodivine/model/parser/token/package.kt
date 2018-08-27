package biodivine.model.parser.token

/**
 * Return a substring (or null if empty) of characters matching [condition] in [this] starting at [position].
 */
internal inline fun String.scanWhile(position: Int, condition: String.(Int, Char) -> Boolean): String? {
    var i = position
    while (i < length && condition(i, this[i])) i += 1
    return if (i == position) null else substring(position until i)
}

/**
 * Try to match regex starting at [position]. If unsuccessful, return null.
 */
internal fun matchRegex(line: String, position: Int, regex: Regex): String? {
    val match = regex.find(line, position)
    return if (match == null || match.range.first != position) null else {
        line.substring(match.range)
    }
}