package dom.sequencer

import dom.htmlparser.HtmlHelper
import dom.htmlparser.HtmlNode

class BbSequencer {
    //private val bbBlocks = arrayOf("IMG", "QUOTE", "CODE")
    private var type: BbOp.Type = BbOp.Type.TEXT
    private val buffer = mutableListOf<BbOp>()
    private val output = mutableListOf<BbTypedOp>()
    private lateinit var rootNodeId: HtmlNode

    fun toSequence(bbNode: HtmlNode): List<BbTypedOp> {
        rootNodeId = bbNode
        printWidgetRec(bbNode)
        releaseBuffer()
        val result = output.toList()
        buffer.clear()
        output.clear()
        return result
    }

    private fun releaseBuffer(bbParents: MutableList<HtmlNode> = mutableListOf()) {
        if (type == BbOp.Type.TEXT) {
            val opened = mutableMapOf<HtmlNode, BbOp>()
            buffer.forEach {
                if (it.op == BbOp.OPEN) {
                    opened[it.node] = it
                } else {
                    opened.remove(it.node)
                }
            }
            opened.forEach {
                buffer.remove(it.value)
            }
        }
        //println("Out: " + buffer.joinToString())
        output.add(BbTypedOp(type, buffer.toList()))
        buffer.clear()
        bbParents.forEach {
            appendBuffer(it)
        }
    }

    private fun appendBuffer(bbNode: HtmlNode) {
        if (bbNode.name == HtmlNode.NODE_TEXT) {
            buffer.add(BbOp(BbOp.APPEND, bbNode))
        } else {
            buffer.add(BbOp(BbOp.OPEN, bbNode))
        }
    }

    private fun closeBuffer(bbNode: HtmlNode) {
        if (bbNode.name == HtmlNode.NODE_TEXT) {

        } else {
            buffer.add(BbOp(BbOp.CLOSE, bbNode))
        }
    }


    private fun printWidgetRec(bbNode: HtmlNode, bbParents: MutableList<HtmlNode> = mutableListOf()) {

        var newBufferType = type
        var bufferReleased = false

        if (bbNode != rootNodeId) {
            bbParents.add(bbNode)
            newBufferType = if (!HtmlHelper.inline.contains(bbNode.name)) BbOp.Type.BLOCK else BbOp.Type.TEXT
            bufferReleased = type != newBufferType


            if (bbParents.isNotEmpty()) {
                if (bufferReleased) {
                    releaseBuffer(bbParents)
                } else {
                    appendBuffer(bbNode)
                }
            }
        }

        type = newBufferType

        if (!bufferReleased) {
            bbNode.nodes?.forEach {
                printWidgetRec(it, bbParents)
            }
        }

        if (bbNode != rootNodeId) {
            if (bbParents.isNotEmpty()) {
                if (bufferReleased) {
                    //releaseBuffer(bbParents)
                } else {
                    //closeBuffer(bbNode)
                }
                if (type == BbOp.Type.TEXT) {
                    closeBuffer(bbNode)
                }
            }
            bbParents.remove(bbNode)
        }
    }

}