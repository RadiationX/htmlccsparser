package dom.sequencer

import dom.htmlparser.HtmlNode

class BbOp(val op: Int, val node: HtmlNode) {
    companion object {
        const val OPEN: Int = 2
        const val APPEND: Int = 4
        const val CLOSE: Int = 8
    }

    enum class Type { TEXT, BLOCK }
}