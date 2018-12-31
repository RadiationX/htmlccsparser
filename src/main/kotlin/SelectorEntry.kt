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
}