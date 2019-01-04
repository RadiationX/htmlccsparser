package dom.htmlparser

import dom.format
import kotlin.math.max

class HtmlDocument(
    val root: HtmlNode
) {
    val unclosedTags = mutableListOf<HtmlNode>()
    var nodesAdded = 0
    var nodesClosed = 0
    var parsingTime = 0L
    var attrTime = 0L
    val allTime
        get() = parsingTime + attrTime


    fun getStatisticInfo(): String {
        val allTimeMillis = allTime / 1000000.0
        val mainTimeMillis = parsingTime / 1000000.0
        val attrTimeMillis = attrTime / 1000000.0


        val allF = max(allTimeMillis, 1.0)
        return "time: all=${allTimeMillis.format(2)}, style=${mainTimeMillis.format(2)}ms(${(mainTimeMillis / allF).format(
            2
        )}%), attrs=${attrTimeMillis.format(
            2
        )}ms(${(attrTimeMillis / allF).format(2)}%)"
    }
}