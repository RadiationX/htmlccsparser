package dom.htmlparser


import dom.map
import dom.mapOnce
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

        private val accumulateStartPattern = Pattern.compile("<[\\w]*([^\\>]+)\$")
        private val accumulateEndPattern = Pattern.compile(">[^<]*\$")
        private val accumulateMainPattern =
            Pattern.compile("\\<(?:(?:(script|style|textarea)(?:([^\\>]+))?\\>)([\\s\\S]*?)(?:\\<\\/\\1)|([\\/])?(!?[\\w]*)(?:([\\s\\S]+))?\\/?)\\>(?:([^<]+))?\$")
    }

    private var attributesTime = 0L
    private var parsingTime = 0L
    private val openedNodes = ArrayList<HtmlNode>()
    private var lastOpened: HtmlNode? = null
    private var attrMatcher: Matcher? = null

    fun parse(html: String): HtmlDocument {
        val entries = getEntries(html)

        val root = HtmlNode(HtmlNode.NODE_DOCUMENT)
        val doc = HtmlDocument(root)
        openedNodes.add(root)

        var accumulateMode = false
        var accumulateSource: String? = null

        val startTime = System.nanoTime()
        for (entry in entries) {
            val special = entry.tag == null && entry.specialTag != null

            val attrs = if (special) entry.specialAttrs else entry.attrs
            val text = if (special) entry.specialText else entry.text

            val accumulateStart = !accumulateMode && (attrs?.let { accumulateStartPattern.matcher(it).find() } ?: false)
            val accumulateEnd = accumulateMode && (text?.let { accumulateEndPattern.matcher(it).find() } ?: false)

            if (accumulateStart) {
                accumulateMode = true
            }
            if (accumulateMode) {
                accumulateSource = (accumulateSource ?: "") + entry.source
            }
            if (accumulateEnd) {
                accumulateSource?.also {
                    parseAndAddAccumulated(it)
                }
                accumulateMode = false
                accumulateSource = null
            }
            if (accumulateMode || accumulateStart || accumulateEnd) {
                continue
            }

            parseAndAddEntry(entry)
        }
        openedNodes.remove(root)
        parsingTime += System.nanoTime() - startTime

        doc.allNodes.addAll(HtmlHelper.getAllNodesList(doc.root, true))
        doc.parsingTime = parsingTime
        doc.attrTime = attributesTime
        return doc
    }

    private fun getEntries(html: String): List<HtmlEntry> {
        return mainPattern.matcher(html).map { HtmlEntry(it) }
    }

    private fun parseAndAddAccumulated(source: String) {
        accumulateMainPattern.matcher(source).mapOnce { HtmlEntry(it) }?.also {
            parseAndAddEntry(it)
        }
    }

    private fun parseAndAddEntry(entry: HtmlEntry) {
        val lastOpened = openedNodes.last()
        this.lastOpened = lastOpened

        val openAction = entry.closingMarker == null

        if (openAction) {
            val node = HtmlNode(HtmlNode.NODE_UNKNOWN)

            val special = entry.tag == null && entry.specialTag != null

            val tagName: String? = if (special) entry.specialTag else entry.tag
            val attrs = if (special) entry.specialAttrs else entry.attrs
            val text = if (special) entry.specialText else entry.text

            var addToStack = true
            if (tagName == null) {
                if (text == null) {
                    node.name = HtmlNode.NODE_COMMENT
                    node.text = entry.source
                }
                addToStack = false
            } else {
                node.name = tagName

                if (attrs != null) {
                    val startTimeAttr = System.nanoTime()
                    val attrMatcher = getMatcher(attrMatcher, attrPattern, attrs)
                    this.attrMatcher = attrMatcher
                    while (attrMatcher.find()) {
                        node.putAttribute(attrMatcher.group(1), attrMatcher.group(3))
                    }
                    attributesTime += System.nanoTime() - startTimeAttr
                }

                if (HtmlHelper.isUnclosing(tagName)) {
                    addToStack = false
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
                        addToStack = false
                    }
                    val textNode = HtmlNode(HtmlNode.NODE_TEXT)
                    textNode.text = correctedText
                    node.addNode(textNode)
                }

            }

            lastOpened.addNode(node)
            if (addToStack) {
                openedNodes.add(node)
            }
        } else {
            openedNodes.remove(lastOpened)
        }
    }

    private fun getMatcher(matcher: Matcher?, pattern: Pattern, text: String): Matcher {
        return if (matcher == null) pattern.matcher(text) else matcher.reset(text)
    }

    class HtmlEntry(matcher: Matcher) {
        var source: String? = null
        var specialTag: String? = null
        var specialAttrs: String? = null
        var specialText: String? = null
        var closingMarker: String? = null
        var tag: String? = null
        var attrs: String? = null
        var text: String? = null

        init {
            source = matcher.group()
            specialTag = matcher.group(S_TAG)
            specialAttrs = matcher.group(S_ATTRS)
            specialText = matcher.group(S_TEXT)
            closingMarker = matcher.group(CLOSING)
            tag = matcher.group(TAG)
            attrs = matcher.group(ATTRS)
            text = matcher.group(TEXT)
        }
    }
}