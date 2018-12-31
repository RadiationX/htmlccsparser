class Cascade(
    val selectors: List<Selector>,
    val attributes: Map<String, String> = emptyMap()
) {
    fun getData(): String {
        val selector = selectors.joinToString(",\n") { it.getData() }
        val attrs = attributes.toList().joinToString("\n") { "    ${it.first}: ${it.second};" }
        return "$selector {\n$attrs\n}"
    }
}