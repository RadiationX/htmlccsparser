class SelectorEntry {
    @JvmField
    var parent: Selector? = null
    @JvmField
    var withSpecify: Specify = Specify.DEFAULT
    @JvmField
    var withNodeTag: String? = null
    @JvmField
    var withNodeId: String? = null
    @JvmField
    var withClasses: MutableList<String>? = null
    @JvmField
    var withAttributes: MutableList<String>? = null
    @JvmField
    var withPseudo: MutableList<String>? = null

    fun addClass(data: String) {
        if (withClasses == null) {
            withClasses = mutableListOf()
        }
        withClasses?.add(data)
    }

    fun addAttr(data: String) {
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

    fun getSelectorEntry(): String {
        val specify = when (withSpecify) {
            Specify.DEFAULT -> withSpecify.text
            else -> "${withSpecify.text} "
        }
        val nodeTag = withNodeTag ?: ""
        val nodeId = withNodeId ?: ""
        val classes = withClasses?.joinToString("") { ".$it" } ?: ""
        val attributes = withAttributes?.joinToString("") { "[$it]" } ?: ""
        val pseudo = withPseudo?.joinToString("") { ":$it" } ?: ""
        return "$specify$nodeTag$nodeId$classes$attributes$pseudo"
    }

    fun getSelectorEntryDetail(): String {
        val specify = withSpecify.text
        val nodeTag = withNodeTag
        val nodeId = withNodeId
        val classes = withClasses?.joinToString("") { ".$it" }
        val attributes = withAttributes?.joinToString("") { "[$it]" }
        val pseudo = withPseudo?.joinToString("") { ":$it" }
        return "spec='$specify', tag='$nodeTag', id='$nodeId', class='$classes', attr='$attributes', pseudo='$pseudo'"
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        (other as? SelectorEntry)?.also { otherEntry ->
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
            if (withAttributes?.all { otherEntry.withAttributes?.contains(it) == true } == false) {
                return false
            }
            if (withPseudo?.all { otherEntry.withPseudo?.contains(it) == true } == false) {
                return false
            }
            return true
        }
        return super.equals(other)
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