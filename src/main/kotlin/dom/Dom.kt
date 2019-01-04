package dom

import dom.cssparser.CssParserTask
import dom.cssparser.models.*
import dom.htmlparser.HtmlDocument
import dom.htmlparser.HtmlHelper
import dom.htmlparser.HtmlNode
import dom.htmlparser.HtmlParser
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.regex.Pattern

class Dom {

    companion object {
        private val startMultiplePattern = Pattern.compile("^(\\w+(?:[-][^\$]*)?)\$")
    }

    init {
        val cssSrc = Utils.loadFile("kekolol.css")
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
        val allDomNodes = htmlDocument.allNodes.map { DomNode(it) }
        allDomNodes.forEach {
            domNodesMap[it.htmlNode] = it
        }
        val domNodes = allDomNodes.filter { !HtmlHelper.isNotElement(it.htmlNode) }
        println("onparsed domnodes = ${domNodes.size}")
        println("onparsed dom = ${htmlDocument.getInfo()}")
        println("onparsed dom = ${htmlDocument.getStatisticInfo()}")
        println("onparsed stylesheet = ${stylesheet.cascades.size}, ${stylesheet.selectors.size}, ${stylesheet.selectorEntries.size}")
        println("onparsed ss = ${stylesheet.getStatisticInfo()}")
        /*println("\ndomnodes")
        domNodes.forEach {
            println(getDomNodePrint(it))
        }*/
        /*println("\ncascades")
        stylesheet.cascades.forEach {
            println(it.getData())
        }
        println("\nselectors")
        stylesheet.selectors.forEach {
            println("  ${it.key} => ${it.value.size}: ${it.value.joinToString { it.getData() }}")
        }
        println("\nentries")
        stylesheet.selectorEntries.forEach {
            println("  ${it.key} => ${it.value.size}: ${it.value.joinToString { it.getSelectorEntry() }}")
        }*/

        println("\n\nFILL CASCADES")
        val time = System.currentTimeMillis()
        fillCascadesBySelector(stylesheet, domNodes)
        println("fill time = ${System.currentTimeMillis() - time}")


        println("\n\nCHECK CASCADES")
        domNodes.forEach {
            println("${getDomNodePrint(it)}")
            val selectors = mutableListOf<CssSelector>()
            it.cascades.forEach {
                selectors.addAll(it.selectors)
            }
            val selectorStr = it.cascades.joinToString(";\n") { it.selectors.joinToString(",\n") { it.getData() } }
            val attrsStr = it.activeCssAttributes.toList().joinToString("\n") { "    ${it.name}: ${it.rawValue};" }
            println("$selectorStr {\n$attrsStr\n}")
            println()
            println()
        }
    }

    private fun fillCascadesBySelector(stylesheet: Stylesheet, domNodes: List<DomNode>) {
        val cascadesBatch = mutableMapOf<DomNode, MutableList<CssCascade>>()
        stylesheet.selectors.forEach {
            val selectors = it.value
            //println("\n\nfill selectors ${selectors.size}")
            for (selector in selectors) {
                //println("fill selector '${selector.getData()}'")
                val entries = selector.entries
                val anchorEntry = entries.lastOrNull { !it.isOnlyPseudo() }
                val anchorHasPseudo = anchorEntry?.withPseudo != null
                val anchorNextHasPseudo = anchorEntry?.next?.withPseudo != null
                if (anchorEntry == null || anchorHasPseudo || anchorNextHasPseudo) {
                    continue
                }
                domNodes.forEach { domNode ->
                    val cascade = tryFillCascadeBySelector(anchorEntry, selector, domNode)
                    if (cascade != null) {
                        val list = cascadesBatch[domNode] ?: (mutableListOf<CssCascade>()).also {
                            cascadesBatch[domNode] = it
                        }
                        list.add(cascade)
                    }
                }
            }
        }
        cascadesBatch.forEach {
            it.key.putCascades(it.value)
        }
    }

    private fun tryFillCascadeBySelector(
        anchorEntry: CssSelectorEntry,
        selector: CssSelector,
        domNode: DomNode
    ): CssCascade? {

        var checkResult = checkEntry(anchorEntry, domNode)

        if (checkResult) {
            var prevCheck = true
            var prevEntry = anchorEntry.prev
            var checkNode: DomNode? = domNode
            while (prevEntry != null && checkNode != null && prevCheck) {
                if (!prevEntry.isOnlyPseudo()) {
                    checkNode = checkPrevEntry(prevEntry, checkNode)
                    prevCheck = checkNode != null
                }
                prevEntry = prevEntry.prev
            }
            checkResult = prevCheck
        }

        if (checkResult) {

            //println("tryFillCascadeByEntry")
            //println("accept: ${getDomNodePrint(domNode)}")
            //println()
            return selector.cascade
        }
        return null
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
        return domNodesMap[checkNode]
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
        val entryNodeId = entry.withNodeId
        val entryNodeTag = entry.withNodeTag
        val entryClasses = entry.withClasses
        val entryAttrs = entry.withAttributes
        var currentAccept = true

        if (entryNodeId != null && currentAccept) {
            currentAccept = htmlNode.attributes?.get("id") == entryNodeId
        }

        if (entryNodeTag != null && currentAccept) {
            currentAccept = htmlNode.name == entryNodeTag || entryNodeTag == "*"
        }

        if (entryClasses != null && currentAccept) {
            val nodeClasses = htmlNode.attributes?.get("class")
            currentAccept = if (nodeClasses != null) {
                entryClasses.all { nodeClasses.contains(it) }
            } else {
                false
            }
        }

        if (entryAttrs != null && currentAccept) {
            currentAccept = entryAttrs.all {
                val nodeAttr = htmlNode.attributes?.get(it.key)
                val attr = it.value.orEmpty()
                if (nodeAttr != null) {
                    when (it.type) {
                        CssSelectorAttribute.Type.EQUALS -> nodeAttr == attr
                        CssSelectorAttribute.Type.CONTAINS -> nodeAttr.contains(attr)
                        CssSelectorAttribute.Type.CONTAINS_MULTIPLE -> nodeAttr.split(' ').contains(attr)
                        CssSelectorAttribute.Type.START_WITH -> nodeAttr.startsWith(attr)
                        CssSelectorAttribute.Type.START_MULTIPLE -> {
                            startMultiplePattern.matcher(nodeAttr).mapOnce {
                                it.group().contains(attr)
                            } == true
                        }
                        CssSelectorAttribute.Type.END_WITH -> nodeAttr.endsWith(attr)
                        null -> true
                    }
                } else {
                    false
                }
            }
        }

        return currentAccept
    }

    private fun getDomNodePrint(node: DomNode): String {
        val attrs = node.htmlNode.attributes?.toList()?.joinToString { "${it.first}=\"${it.second}\"" }
        return "<${node.htmlNode.name}${attrs?.let { " $it" } ?: ""}>"
    }
}