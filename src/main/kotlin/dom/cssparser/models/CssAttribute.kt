package dom.cssparser.models

class CssAttribute(
    val name: String,
    val rawValue: String
) {
    var important: Boolean = false
}