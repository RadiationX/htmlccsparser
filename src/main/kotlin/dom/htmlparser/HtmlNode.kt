package dom.htmlparser

import java.util.LinkedHashMap

/**
 * Created by radiationx on 13.08.17.
 */

open class HtmlNode {

    var nodes: MutableList<HtmlNode>? = null
    var attributes: LinkedHashMap<String, String>? = null
    var name: String? = null
    var text: String? = null

    constructor() {}

    constructor(name: String) {
        this.name = name
    }

    fun addNode(node: HtmlNode) {
        val addedNodes = nodes ?: (mutableListOf<HtmlNode>()).also {
            nodes = it
        }
        addedNodes.add(node)
    }

    override fun toString(): String {
        return "" + name!!
    }

    fun putAttribute(name: String, value: String) {
        val addedAttributes = attributes ?: (LinkedHashMap<String, String>()).also {
            attributes = it
        }
        addedAttributes[name] = value
    }

    companion object {
        val NODE_DOCUMENT = "#document"
        val NODE_TEXT = "#text"
        val NODE_COMMENT = "#comment"
    }
}