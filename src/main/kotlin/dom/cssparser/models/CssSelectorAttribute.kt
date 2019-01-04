package dom.cssparser.models

class CssSelectorAttribute(
    val key: String,
    val value: String?,
    val type: Type?
) {

    fun getData(): String {
        return "[$key${type?.text ?: ""}${value?.let { "=\"$it\"" } ?: ""}]"
    }

    /*override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }

        (other as? CssSelectorAttribute)?.also {
            if (key != it.key) return false
            if (value != it.value) return false
            if (type != it.type) return false
            return true
        }

        return super.equals(other)
    }*/

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CssSelectorAttribute

        if (key != other.key) return false
        if (value != other.value) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        return result
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