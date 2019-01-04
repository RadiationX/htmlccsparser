package dom.cssparser.models

class CssSelectorAttribute(
    val key: String,
    val value: String?,
    val type: Type?
) {

    fun getData(): String {
        return "[$key${type?.text ?: ""}${value?.let { "=\"$it\"" } ?: ""}]"
    }

    enum class Type(val text: String) {
        EQUALS(""),
        CONTAINS("*"),
        CONTAINS_MULTIPLE("~"),
        START_WITH("^"),
        START_MULTIPLE("|"),
        END_WITH("\$")
    }
}