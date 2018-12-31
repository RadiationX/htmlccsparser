class SelectorEntry(
    var parent: Selector? = null,
    var withSpecify: Specify = Specify.DEFAULT,
    var withNodeTag: String? = null,
    var withNodeId: String? = null,
    var withClasses: MutableList<String> = mutableListOf<String>(),
    var withAttributes: MutableList<String> = mutableListOf<String>(),
    var withPseudo: MutableList<String> = mutableListOf<String>()
) {
    fun getSelectorEntry(): String {
        val specify = when (withSpecify) {
            Specify.DEFAULT -> withSpecify.text
            else -> "${withSpecify.text} "
        }
        val nodeTag = withNodeTag ?: ""
        val nodeId = withNodeId ?: ""
        val classes = withClasses.joinToString("") { ".$it" }
        val attributes = withAttributes.joinToString("") { "[$it]" }
        val pseudo = withPseudo.joinToString("") { ":$it" }
        return "$specify$nodeTag$nodeId$classes$attributes$pseudo"
    }

    fun getSelectorEntryDetail(): String {
        val specify = withSpecify.text
        val nodeTag = withNodeTag
        val nodeId = withNodeId
        val classes = withClasses.joinToString("") { ".$it" }
        val attributes = withAttributes.joinToString("") { "[$it]" }
        val pseudo = withPseudo.joinToString("") { ":$it" }
        return "spec='$specify', tag='$nodeTag', id='$nodeId', class='$classes', attr='$attributes', pseudo='$pseudo'"
    }
}