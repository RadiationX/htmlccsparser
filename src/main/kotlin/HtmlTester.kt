import dom.htmlparser.HtmlHelper
import dom.htmlparser.HtmlParser
import dom.sequencer.BbSequencer

class HtmlTester {

    init {
        val text = Utils.loadFile("simple.html")
        HtmlParser().parse(text)
        val doc = HtmlParser().parse(text)
        //println(HtmlHelper.getHtml(doc.root, false))
        println(doc.getStatisticInfo())
        println(doc.getInfo())
        val sequence = BbSequencer().toSequence(doc.root)
        sequence.forEach {
            println("seq: ${it.type}")
            it.bbOps.forEach {
                println("el: ${it.node.name} '${it.node.attributes}' '${it.node.text}'")
            }
        }
    }
}