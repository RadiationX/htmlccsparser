package dom.htmlparser

class HtmlDocument : HtmlNode(HtmlNode.NODE_DOCUMENT) {
    var docType = "html"

    companion object {
        const val DOCTYPE_TAG = "!DOCTYPE"
    }
}