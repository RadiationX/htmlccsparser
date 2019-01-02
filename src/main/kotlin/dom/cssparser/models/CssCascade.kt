package dom.cssparser.models

class CssCascade(
    @JvmField
    val selectors: List<CssSelector>,
    @JvmField
    val attributes: Map<String, String> = emptyMap()
) {

    init {
        selectors.forEach { it.cascade = this }
    }

    fun getData(): String {
        val selector = selectors.joinToString(",\n") { it.getData() }
        val attrs = attributes.toList().joinToString("\n") { "    ${it.first}: ${it.second};" }
        return "$selector {\n$attrs\n}"
    }
}