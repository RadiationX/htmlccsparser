import dom.htmlparser.HtmlHelper
import dom.htmlparser.HtmlParser
import dom.sequencer.BbSequencer

class HtmlTester {

    init {
        val text = Utils.loadFile("notsimple.html")
        HtmlParser().parse(text)
        val doc = HtmlParser().parse(text)
        //println(HtmlHelper.getHtml(doc.root, false))
        println(doc.getStatisticInfo())
        println(doc.getInfo())
        val node = HtmlHelper.findNode(doc.root, "body") ?: doc.root
        val sequence = BbSequencer().toSequence(node)
        sequence.forEach {
            println("seq: ${it.type}")
            it.bbOps.forEach {
                println("el: ${it.node.name} '${it.node.attributes}' '${it.node.text}'")
            }
        }
    }
}