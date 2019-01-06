package dom.htmlparser

import dom.format
import kotlin.math.max

class HtmlDocument(
    val root: HtmlNode
) {
    val unclosedTags = mutableListOf<HtmlNode>()
    val allNodes = mutableListOf<HtmlNode>()
    var allEntries = 0
    var parsingTime = 0L
    var attrTime = 0L
    val allTime
        get() = parsingTime + attrTime


    fun getInfo(): String {
        return "unclosed=${unclosedTags.size}, allnodes=${allNodes.size}, allentries=$allEntries"
    }

    fun getStatisticInfo(): String {
        val fixAllTime = max(allTime, 1)
        val allTimeMillis = allTime / 1000000.0
        val mainTimeMillis = parsingTime / 1000000.0
        val attrTimeMillis = attrTime / 1000000.0
        val allF = fixAllTime / 1000000.0

        return "time: all=${allTimeMillis.format(2)}, style=${mainTimeMillis.format(2)}ms(${(mainTimeMillis / allF).format(
            2
        )}%), attrs=${attrTimeMillis.format(
            2
        )}ms(${(attrTimeMillis / allF).format(2)}%)"
    }
}