package dom.cssparser.models

class CssSelectorEntry {
    @JvmField
    var parent: CssSelector? = null
    @JvmField
    var prev: CssSelectorEntry? = null
    @JvmField
    var next: CssSelectorEntry? = null

    @JvmField
    var withSpecify: CssSpecify = CssSpecify.DEFAULT
    @JvmField
    var withNodeTag: String? = null
    @JvmField
    var withNodeId: String? = null
    @JvmField
    var withClasses: MutableList<String>? = null
    @JvmField
    var withAttributes: MutableList<CssSelectorAttribute>? = null
    @JvmField
    var withPseudo: MutableList<String>? = null

    fun addClass(data: String) {
        if (withClasses == null) {
            withClasses = mutableListOf()
        }
        withClasses?.add(data)
    }

    fun addAttr(data: CssSelectorAttribute) {
        if (withAttributes == null) {
            withAttributes = mutableListOf()
        }
        withAttributes?.add(data)
    }

    fun addPseudo(data: String) {
        if (withPseudo == null) {
            withPseudo = mutableListOf()
        }
        withPseudo?.add(data)
    }

    fun isOnlyPseudo(): Boolean {
        if (withPseudo != null && withSpecify == CssSpecify.DEFAULT && withNodeTag == null && withNodeId == null && withClasses == null && withAttributes == null) {
            return true
        }
        return false
    }

    fun isOnlyAttribute(): Boolean {
        if (withAttributes != null && withSpecify == CssSpecify.DEFAULT && withNodeTag == null && withNodeId == null && withClasses == null && withPseudo == null) {
            return true
        }
        return false
    }

    fun isPseudoOrAttribute(): Boolean {
        if ((withAttributes != null || withPseudo != null) && withSpecify == CssSpecify.DEFAULT && withNodeTag ==
            null && withNodeId == null && withClasses == null
        ) {
            return true
        }
        return false
    }

    fun getSelectorEntry(): String {
        val specify = when (withSpecify) {
            CssSpecify.DEFAULT -> withSpecify.text
            else -> "${withSpecify.text} "
        }
        val nodeTag = withNodeTag ?: ""
        val nodeId = withNodeId ?: ""
        val classes = withClasses?.joinToString("") { ".$it" } ?: ""
        val attributes = withAttributes?.joinToString("") { it.getData() } ?: ""
        val pseudo = withPseudo?.joinToString("") { ":$it" } ?: ""
        return "$specify$nodeTag$nodeId$classes$attributes$pseudo"
    }

    fun getSelectorEntryDetail(): String {
        val specify = withSpecify.text
        val nodeTag = withNodeTag
        val nodeId = withNodeId
        val classes = withClasses?.joinToString("") { ".$it" }
        val attributes = withAttributes?.joinToString("") { it.getData() }
        val pseudo = withPseudo?.joinToString("") { ":$it" }
        return "spec='$specify', tag='$nodeTag', id='$nodeId', class='$classes', attr='$attributes', pseudo='$pseudo'"
    }

  /*  override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        (other as? CssSelectorEntry)?.also { otherEntry ->
            if ((withClasses?.size ?: 0) != (otherEntry.withClasses?.size ?: 0)) {
                return false
            }
            if ((withAttributes?.size ?: 0) != (otherEntry.withAttributes?.size ?: 0)) {
                return false
            }
            if ((withPseudo?.size ?: 0) != (otherEntry.withPseudo?.size ?: 0)) {
                return false
            }
            if (withSpecify != otherEntry.withSpecify) {
                return false
            }
            if (withNodeTag != otherEntry.withNodeTag) {
                return false
            }
            if (withNodeId != otherEntry.withNodeId) {
                return false
            }
            if (withClasses?.all { otherEntry.withClasses?.contains(it) == true } == false) {
                return false
            }
            val attrsSize = withAttributes?.size ?: 0
            if ((0 until attrsSize).all { withAttributes?.get(it) == otherEntry.withAttributes?.get(it) }) {
                return false
            }
            if (withPseudo?.all { otherEntry.withPseudo?.contains(it) == true } == false) {
                return false
            }
            return true
        }
        return super.equals(other)
    }*/


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CssSelectorEntry

        if (parent != other.parent) return false
        if (prev != other.prev) return false
        if (next != other.next) return false
        if (withSpecify != other.withSpecify) return false
        if (withNodeTag != other.withNodeTag) return false
        if (withNodeId != other.withNodeId) return false
        if (withClasses != other.withClasses) return false
        if (withAttributes != other.withAttributes) return false
        if (withPseudo != other.withPseudo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = withSpecify.hashCode()
        result = 31 * result + (withNodeTag?.hashCode() ?: 0)
        result = 31 * result + (withNodeId?.hashCode() ?: 0)
        result = 31 * result + (withClasses?.hashCode() ?: 0)
        result = 31 * result + (withAttributes?.hashCode() ?: 0)
        result = 31 * result + (withPseudo?.hashCode() ?: 0)
        return result
    }
}