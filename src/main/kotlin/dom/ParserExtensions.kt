package dom

import java.util.regex.Matcher

fun String.trimWhiteSpace(): String {
    var startIndex = 0
    var endIndex = length - 1
    var startFound = false

    while (startIndex <= endIndex) {
        val index = if (!startFound) startIndex else endIndex
        val match = this[index].isWhitespace()


        if (!startFound) {
            if (!match)
                startFound = true
            else
                startIndex += 1
        } else {
            if (!match)
                break
            else
                endIndex -= 1
        }
    }

    return this.substring(startIndex, endIndex + 1)
}

inline fun Matcher.findOnce(action: (Matcher) -> Unit): Matcher {
    if (this.find()) action(this)
    return this
}

inline fun Matcher.findAll(action: (Matcher) -> Unit): Matcher {
    while (this.find()) action(this)
    return this
}

inline fun <R> Matcher.map(transform: (Matcher) -> R): List<R> {
    val data = mutableListOf<R>()
    findAll {
        data.add(transform(this))
    }
    return data
}

inline fun <R> Matcher.mapOnce(transform: (Matcher) -> R): R? {
    var data: R? = null
    findOnce {
        data = transform(this)
    }
    return data
}

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)