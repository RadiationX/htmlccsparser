package dom.cssparser.models

enum class CssSpecify(val text: String) {
    DEFAULT(""),
    INSIDE(">"),
    NEXT("+"),
    ALL_NEXT("~")
}