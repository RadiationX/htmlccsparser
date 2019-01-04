package dom.htmlparser

import java.util.ArrayList
import java.util.regex.Matcher

object HtmlHelper {
    val unclosing = arrayOf(
        "!doctype",
        "area",
        "br",
        "col",
        "colgroup",
        "command",
        "embed",
        "hr",
        "img",
        "input",
        "keygen",
        "link",
        "meta",
        "param",
        "source",
        "track",
        "wbr"
    )

    val inline = arrayOf(
        "b",
        "big",
        "i",
        "small",
        "tt",
        "abbr",
        "acronym",
        "cite",
        "code",
        "dfn",
        "em",
        "kbd",
        "strong",
        "samp",
        "var",
        "a",
        "bdo",
        "br",
        "img",
        "map",
        "object",
        "q",
        "script",
        "span",
        "sub",
        "sup",
        "button",
        "input",
        "label",
        "select",
        "textarea"
    )

    fun isUnclosing(tag: String?): Boolean {
        return isExistTag(tag, unclosing)
    }

    fun isInline(tag: String?): Boolean {
        return isExistTag(tag, inline)
    }

    fun isSourceInline(isInline: Boolean, tag: String?): Boolean {
        val correctedTag = tag?.toLowerCase()
        return isInline && correctedTag != "script"
    }

    private fun isExistTag(tag: String?, array: Array<String>): Boolean {
        val correctedTag = tag?.toLowerCase()
        return array.any { it.equals(correctedTag, true) }
    }

    fun isNotElement(node: HtmlNode): Boolean {
        return node.name == null || node.name == HtmlNode.NODE_TEXT || node.name == HtmlNode.NODE_COMMENT
    }

    fun isTextNode(node: HtmlNode): Boolean {
        return node.name == HtmlNode.NODE_TEXT
    }

    fun getAllNodesList(
        node: HtmlNode,
        withoutDocument: Boolean = false,
        result: MutableList<HtmlNode> = mutableListOf()
    ): List<HtmlNode> {
        if (!withoutDocument || node.name != HtmlNode.NODE_DOCUMENT) {
            result.add(node)
        }
        node.nodes?.forEach {
            getAllNodesList(it, withoutDocument, result)
        }
        return result
    }

    fun getHtml(document: HtmlDocument, node: HtmlNode, matcher: Matcher): String {
        val resultHtml = StringBuilder()
        val onlyText = isNotElement(node)

        if (onlyText) {
            resultHtml.append(node.text)
        } else {
            resultHtml.append("<").append(node.name)
            node.attributes?.forEach {
                resultHtml.append(" ").append(it.key).append("=\"").append(it.value).append("\"")
            }
            if (node.attributes != null) {

            } else {
                /*if (node.name.equals(HtmlDocument.DOCTYPE_TAG, ignoreCase = true)) {
                    resultHtml.append(" ").append(document.docType)
                }*/
            }
            resultHtml.append(">")
        }

        if (!onlyText) {
            node.nodes?.forEach {
                val s = getHtml(document, it, matcher)
                resultHtml.append(s)
            }
        }


        if (!onlyText) {
            if (!isUnclosing(node.name)) {
                resultHtml.append("</").append(node.name).append(">")
            }
        }


        return resultHtml.toString()
    }

    fun getHtml(node: HtmlNode, onlyInner: Boolean = false, level: Int = 0, parentNode: HtmlNode? = null): String? {
        if (isNotElement(node)) {
            return node.text
        }
        val resultHtml = StringBuilder()
        val isInline = HtmlHelper.isInline(node.name)
        val isSourceInline = HtmlHelper.isSourceInline(isInline, node.name)
        val isParentInline = HtmlHelper.isInline(parentNode?.name)

        if (!onlyInner) {
            if (!isParentInline || !isSourceInline) {
                resultHtml.append("\n")
                (0 until level).forEach { _ ->
                    resultHtml.append("  ")
                }
            }


            resultHtml.append("<").append(node.name)
            if (node.attributes != null) {
                node.attributes?.forEach {
                    resultHtml.append(" ").append(it.key).append("=\"").append(it.value).append("\"")
                }
            }
            resultHtml.append(">")
        }


        var currentParent: HtmlNode = node
        node.nodes?.forEach { child ->
            val s = getHtml(child, false, level + 1, currentParent)
            if (!isNotElement(child)) {
                currentParent = child
            }
            resultHtml.append(s)
        }


        if (!onlyInner) {
            if (!isUnclosing(node.name)) {
                if (!isInline) {
                    resultHtml.append("\n")
                }
                if (!isInline) {
                    (0 until level).forEach { _ ->
                        resultHtml.append("  ")
                    }
                }

                resultHtml.append("</").append(node.name).append(">")
                /*if (!isInline) {
                    resultHtml.append("\n")
                }*/
            }
        }


        return resultHtml.toString()
    }


    fun findNode(node: HtmlNode, tag: String, attr: String?, value: String): HtmlNode? {
        if (isNotElement(node)) {
            return null
        }
        if (node.name.equals(tag, ignoreCase = true)) {
            if (attr == null) {
                return node
            }
            val attrValue = node.attributes?.get(attr)
            if (attrValue != null && attrValue.contains(value)) {
                return node
            }
        }
        var result: HtmlNode? = null
        val nodes = node.nodes
        if (nodes != null) {
            for (child in nodes) {
                result = findNode(child, tag, attr, value)
                if (result != null)
                    break
            }
        }
        return result
    }

    fun findChildNodes(node: HtmlNode, tag: String, attr: String?, value: String): ArrayList<HtmlNode> {
        val result = ArrayList<HtmlNode>()
        if (isNotElement(node)) {
            return result
        }

        val nodes = node.nodes
        if (nodes != null) {
            for (child in nodes) {
                if (isNotElement(child))
                    continue
                if (child.name.equals(tag, ignoreCase = true)) {
                    if (attr == null) {
                        result.add(child)
                        continue
                    }
                    val attrValue = child.attributes?.get(attr)
                    if (attrValue != null && attrValue.contains(value)) {
                        result.add(child)
                    }
                }
            }
        }

        return result
    }

    fun ownText(node: HtmlNode): String {
        val stringBuilder = StringBuilder()

        node.nodes?.forEach {
            if (isTextNode(it)) {
                stringBuilder.append(it.text)
            }
        }
        return stringBuilder.toString()
    }
}