import dom.htmlparser.HtmlHelper
import dom.htmlparser.HtmlParser

class HtmlTester {

    init {
        val text = Utils.loadFile("test.html")
        val doc = HtmlParser().parse(text)
        println(HtmlHelper.getHtml(doc, false))
    }
}