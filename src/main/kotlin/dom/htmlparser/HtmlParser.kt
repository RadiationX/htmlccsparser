package dom.htmlparser


import dom.trimWhiteSpace
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 13.08.17.
 */

class HtmlParser {
    companion object {

        private const val S_TAG = 1
        private const val S_ATTRS = 2
        private const val S_TEXT = 3
        private const val CLOSING = 4
        private const val TAG = 5
        private const val ATTRS = 6
        private const val TEXT = 7

        /*
         * GROUPS
         *
         * script/style/textarea/etc:
         * 1. Tag name
         * 2. Attributes
         * 3. Inner text
         *
         * basic:
         * 4. Close tag symbol "/"
         * 5. Tag name
         * 6. Attributes
         * 7. Text
         *
         * if no groups - comment
         * */
        private const val mainPatternSrc =
            "\\<(?:(?:(script|style|textarea)(?:([^\\>]+))?\\>)([\\s\\S]*?)(?:\\<\\/\\1)|([\\/])?(!?[\\w]*)(?:([^\\>]+))?\\/?)\\>(?:([^<]+))?"
        private const val attrPatternSrc = "([^ \"']*?)\\s*?=\\s*?([\"'])([\\s\\S]*?)\\2"

        private val mainPattern by lazy { Pattern.compile(mainPatternSrc) }
        private val attrPattern by lazy { Pattern.compile(attrPatternSrc) }
    }

    private fun getMatcher(matcher: Matcher?, pattern: Pattern, text: String): Matcher {
        return if (matcher == null) pattern.matcher(text) else matcher.reset(text)
    }

    fun parse(html: String): HtmlDocument {
        var attributesTime = 0L
        var parsingTime = 0L

        val openedNodes = ArrayList<HtmlNode>()
        val root = HtmlNode(HtmlNode.NODE_DOCUMENT)
        val doc = HtmlDocument(root)

        openedNodes.add(root)
        var lastOpened: HtmlNode? = null

        val matcher = mainPattern.matcher(html)
        var attrMatcher: Matcher? = null
        var nodesAdd = 0
        var nodesClose = 0

        val startTime = System.nanoTime()
        while (matcher.find()) {
            lastOpened = openedNodes[openedNodes.size - 1]
            val node = HtmlNode(HtmlNode.NODE_UNKNOWN)


            var special = false
            var tagName: String? = matcher.group(TAG)
            if (tagName == null) {
                special = true
            }


            val openAction = matcher.group(CLOSING) == null


            if (openAction) {
                if (special) {
                    tagName = matcher.group(S_TAG)
                    special = tagName != null
                }
                val attrs = matcher.group(if (special) S_ATTRS else ATTRS)
                val text = matcher.group(if (special) S_TEXT else TEXT)
                // Log.d("PARSER", "Open last= " + lastOpened + "; new= " + tagName + "; text= '" + text + "'");
                var addToOpened = true
                if (tagName == null) {
                    if (text == null) {
                        node.name = HtmlNode.NODE_COMMENT
                        node.text = matcher.group()
                    }
                    addToOpened = false
                } else {
                    node.name = tagName

                    if (attrs != null) {
                        val startTimeAttr = System.nanoTime()
                        attrMatcher = getMatcher(attrMatcher, attrPattern, attrs)
                        while (attrMatcher.find()) {
                            node.putAttribute(attrMatcher.group(1), attrMatcher.group(3))
                        }
                        attributesTime += System.nanoTime() - startTimeAttr
                    }

                    if (HtmlHelper.isUnclosing(tagName)) {
                        addToOpened = false
                    }

                    val correctedText: String? = if (HtmlHelper.isInline(tagName)) {
                        text
                    } else {
                        val trimmed = text?.trimWhiteSpace()
                        if (trimmed.isNullOrEmpty()) {
                            null
                        } else {
                            trimmed
                        }
                    }
                    if (correctedText != null) {
                        if (special) {
                            addToOpened = false
                        }
                        val textNode = HtmlNode(HtmlNode.NODE_TEXT)
                        textNode.text = correctedText
                        node.addNode(textNode)
                        nodesAdd++
                    }

                }

                lastOpened.addNode(node)
                //Log.d("PARSER", "ADD? = " + addToOpened);
                nodesAdd++
                if (addToOpened) {
                    openedNodes.add(node)
                }
            } else {
                //Log.e("PARSER", "Close last = " + lastOpened);
                openedNodes.remove(lastOpened)
                nodesClose++
            }

        }
        openedNodes.remove(root)
        parsingTime += System.nanoTime() - startTime

        doc.unclosedTags.addAll(openedNodes)
        doc.allNodes.addAll(HtmlHelper.getAllNodesList(doc.root, true))
        doc.nodesAdded = nodesAdd
        doc.nodesClosed = nodesClose
        doc.parsingTime = parsingTime
        doc.attrTime = attributesTime
        return doc
    }

}