package dom

import dom.cssparser.CssParserTask
import dom.cssparser.models.CssSelector
import dom.cssparser.models.CssSelectorEntry
import dom.cssparser.models.CssSpecify
import dom.cssparser.models.Stylesheet
import dom.htmlparser.HtmlDocument
import dom.htmlparser.HtmlHelper
import dom.htmlparser.HtmlNode
import dom.htmlparser.HtmlParser
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class Dom {

    init {
        val cssSrc = Utils.loadFile("kekolol.txt")
        val htmlSrc = Utils.loadFile("test.html")
        val cssObs = BehaviorSubject.create<Stylesheet>()
        val htmlObs = BehaviorSubject.create<HtmlDocument>()

        Observable
            .combineLatest(
                cssObs,
                htmlObs,
                BiFunction<Stylesheet, HtmlDocument, Pair<Stylesheet, HtmlDocument>> { t1, t2 ->
                    Pair(t1, t2)
                }
            )
            .subscribe {
                onParsed(it.first, it.second)
            }

        Single
            .fromCallable {
                HtmlParser().parse(htmlSrc)
            }
            .subscribeOn(Schedulers.computation())
            .subscribe({
                htmlObs.onNext(it)
            }, { it.printStackTrace() })

        RxCssParserTask(cssSrc).parse(object : CssParserTask.StylesheetCallback {
            override fun onSuccess(stylesheet: Stylesheet) {
                cssObs.onNext(stylesheet)
            }

            override fun onError(throwable: Throwable) {
                println("onError ${throwable.message}")
            }
        })
    }

    private val domNodesMap = mutableMapOf<HtmlNode, DomNode>()

    private fun onParsed(stylesheet: Stylesheet, htmlDocument: HtmlDocument) {
        val allHtmlNodes = HtmlHelper
            .getAllNodesList(htmlDocument, true)
        val allDomNodes = allHtmlNodes.map { DomNode(it) }
        allDomNodes.forEach {
            domNodesMap[it.htmlNode] = it
        }
        val domNodes = allDomNodes.filter { !HtmlHelper.isNotElement(it.htmlNode) }
        println("onparsed domnodes = ${domNodes.size}")
        println("onparsed stylesheet = ${stylesheet.cascades.size}, ${stylesheet.selectors.size}, ${stylesheet.selectorEntries.size}")
        println("\ndomnodes")
        domNodes.forEach {
            println(getDomNodePrint(it))
        }
        println("\nselectors")
        stylesheet.selectors.forEach {
            println("  ${it.key} => ${it.value.size}: ${it.value.joinToString { it.getData() }}")
        }
        println("\nentries")
        stylesheet.selectorEntries.forEach {
            println("  ${it.key} => ${it.value.size}: ${it.value.joinToString { it.getSelectorEntry() }}")
        }

        println("\n\nFILL CASCADES")
        fillCascadesBySelector(stylesheet, domNodes)
    }

    private fun fillCascadesBySelector(stylesheet: Stylesheet, domNodes: List<DomNode>) {
        stylesheet.selectors.forEach {
            val selectors = it.value
            println("\n\nfill selectors ${selectors.size}")
            selectors.forEach { selector ->
                println("fill selector '${selector.getData()}'")
                domNodes.forEach { domNode ->
                    tryFillCascadeBySelector(selector, domNode)
                }
            }
        }
    }

    private fun tryFillCascadeBySelector(selector: CssSelector, domNode: DomNode) {
        val entries = selector.entries
        val lastEntry = entries.lastOrNull { !it.isPseudoOrAttribute() }
        var checkResult = lastEntry?.let { checkEntry(it, domNode) } ?: false



        if (checkResult) {
            var prevCheck = true
            var prevEntry = lastEntry?.prev
            var checkNode: DomNode? = domNode
            while (prevEntry != null && checkNode != null && prevCheck) {
                if (!prevEntry.isPseudoOrAttribute()) {
                    checkNode = checkPrevEntry(prevEntry, checkNode)
                    prevCheck = checkNode != null
                }
                prevEntry = prevEntry.prev
            }
            checkResult = prevCheck
        }

        if (checkResult) {


            //println("tryFillCascadeByEntry")
            println("accept: ${getDomNodePrint(domNode)}")
            //println()
        }
    }

    private fun checkPrevEntry(entry: CssSelectorEntry, domNode: DomNode): DomNode? {
        val next = entry.next!!
        val result = when (next.withSpecify) {
            CssSpecify.DEFAULT -> checkPrevEntryDefault(entry, domNode)
            CssSpecify.INSIDE -> checkPrevEntryInside(entry, domNode)
            CssSpecify.NEXT -> checkPrevEntryNext(entry, domNode)
            CssSpecify.ALL_NEXT -> checkPrevEntryAllNext(entry, domNode)
        }
        return result
    }

    private fun checkPrevEntryDefault(entry: CssSelectorEntry, domNode: DomNode): DomNode? {
        var checkNode = domNode.htmlNode.parent
        while (checkNode != null) {
            val checkDomNode = domNodesMap[checkNode] ?: break
            val checked = checkEntry(entry, checkDomNode)
            if (checked) {
                break
            }
            checkNode = checkNode.parent
        }
        val result = domNodesMap[checkNode]
        /*println("checkPrevEntryDefault: e='${entry.getSelectorEntry()}', '${getDomNodePrint(domNode)}'  => ${result?.let {
            getDomNodePrint(
                result
            )
        }}")*/
        return result
    }

    private fun checkPrevEntryInside(entry: CssSelectorEntry, domNode: DomNode): DomNode? {
        val checkNode = domNode.htmlNode.parent
        val checkDomNode = domNodesMap[checkNode]
        checkDomNode?.also {
            if (checkEntry(entry, it)) {
                return it
            }
        }
        return null
    }

    private fun checkPrevEntryNext(entry: CssSelectorEntry, domNode: DomNode): DomNode? {
        val parentHtmlNode = domNode.htmlNode.parent
        val nodes = parentHtmlNode?.nodes ?: return null
        val startIndex = nodes.indexOf(domNode.htmlNode)
        if (startIndex < 1) return null
        (startIndex downTo 0).forEach { index ->
            val node = nodes[index]
            if (!HtmlHelper.isNotElement(node)) {
                val checkNode = domNodesMap[node]
                if (checkNode != null && checkEntry(entry, checkNode)) {
                    return checkNode
                }
            }
        }
        return null
    }

    private fun checkPrevEntryAllNext(entry: CssSelectorEntry, domNode: DomNode): DomNode? {
        val parentHtmlNode = domNode.htmlNode.parent
        val nodes = parentHtmlNode?.nodes ?: return null
        val startIndex = nodes.indexOf(domNode.htmlNode)
        if (startIndex < 1) return null
        var resultNode: DomNode? = null
        (startIndex downTo 0).forEach { index ->
            val node = nodes[index]
            if (!HtmlHelper.isNotElement(node)) {
                val checkNode = domNodesMap[node]
                if (checkNode != null && checkNode != domNode && checkEntry(entry, checkNode)) {
                    resultNode = checkNode
                }
            }
        }
        return resultNode
    }

    private fun checkEntry(entry: CssSelectorEntry, domNode: DomNode): Boolean {
        val htmlNode = domNode.htmlNode
        var currentAccept = false

        entry.withNodeId?.also {
            if (htmlNode.attributes?.get("id") == it) {
                currentAccept = true
            }
        }
        entry.withNodeTag?.also {
            if (htmlNode.name == it) {
                currentAccept = true
            }
        }
        val nodeClasses = htmlNode.attributes?.get("class")
        if (nodeClasses != null && entry.withClasses != null) {
            if (entry.withClasses?.all { nodeClasses.contains(it) } == true) {
                currentAccept = true
            }
        }
        return currentAccept
    }


    private fun fillCascadesByEntry(stylesheet: Stylesheet, domNodes: List<DomNode>) {
        stylesheet.selectorEntries.forEach {
            val entries = it.value
            println("\nfill entries ${entries.size}")
            entries.forEach { entry ->
                println("fill entry ${entry.getSelectorEntry()}")
                domNodes.forEach { domNode ->
                    tryFillCascadeByEntry(entry, domNode)
                }
            }
        }
    }

    private fun tryFillCascadeByEntry(entry: CssSelectorEntry, domNode: DomNode, level: Int = 0) {
        val htmlNode = domNode.htmlNode
        var currentAccept = false

        when (entry.withSpecify) {
            CssSpecify.DEFAULT -> {

            }
            CssSpecify.INSIDE -> {

            }
            CssSpecify.NEXT -> {

            }
            CssSpecify.ALL_NEXT -> {

            }
        }

        entry.withNodeId?.also {
            if (htmlNode.attributes?.get("id") == it) {
                currentAccept = true
            }
        }
        entry.withNodeTag?.also {
            if (htmlNode.name == it) {
                currentAccept = true
            }
        }
        val nodeClasses = htmlNode.attributes?.get("class")
        if (nodeClasses != null && entry.withClasses != null) {
            if (entry.withClasses?.any { nodeClasses.contains(it) } == true) {
                currentAccept = true
            }
        }
        entry.next?.also {
            if (!it.isPseudoOrAttribute()) {
                currentAccept = false
            }
        }
        if (currentAccept) {
            println("tryFillCascadeByEntry, level=$level")
            println("${entry.getSelectorEntry()} => ${getDomNodePrint(domNode)}")
            println()
        }
    }

    private fun getDomNodePrint(node: DomNode): String {
        return "<${node.htmlNode.name} ${node.htmlNode.attributes?.toList()?.joinToString { "${it.first}=\"${it.second}\"" }
            ?: ""}>"
    }
}