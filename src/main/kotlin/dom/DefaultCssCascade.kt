package dom

import dom.cssparser.models.CssAttribute
import dom.cssparser.models.CssCascade
import dom.cssparser.models.CssSelector
import dom.cssparser.models.CssSelectorEntry

object DefaultCssCascade {
    private val cascades = mutableMapOf<String, CssCascade>()
    private val internalColor = "#ff0000"

    private val defaultBlockAttributes = listOf(
        CssAttribute("display", "block")
    )

    init {

        putCascade(
            "a", listOf(
                CssAttribute("color", internalColor),
                CssAttribute("text-decoration", "underline"),
                CssAttribute("cursor", "auto")
            )
        )

        putCascade(
            "address", listOf(
                CssAttribute("display", "block"),
                CssAttribute("font-style", "italic")
            )
        )

        putCascade(
            "area", listOf(
                CssAttribute("display", "none")
            )
        )

        putCascade(
            "article", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "aside", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "b", listOf(
                CssAttribute("font-weight", "bold")
            )
        )

        putCascade(
            "bdo", listOf(
                CssAttribute("unicode-bidi", "bidi-override")
            )
        )

        putCascade(
            "blockquote", listOf(
                CssAttribute("display", "block"),
                CssAttribute("margin-top", "1em"),
                CssAttribute("margin-bottom", "1em"),
                CssAttribute("margin-left", "40px"),
                CssAttribute("margin-right", "40px")
            )
        )

        putCascade(
            "body", listOf(
                CssAttribute("display", "block"),
                CssAttribute("margin", "8px")
            )
        )

        putCascade(
            "caption", listOf(
                CssAttribute("display", "table-caption"),
                CssAttribute("text-align", "center")
            )
        )

        putCascade(
            "cite", listOf(
                CssAttribute("font-style", "italic")
            )
        )

        putCascade(
            "code", listOf(
                CssAttribute("font-family", "monospace")
            )
        )

        putCascade(
            "col", listOf(
                CssAttribute("display", "table-column")
            )
        )

        putCascade(
            "colgroup", listOf(
                CssAttribute("display", "table-column-group")
            )
        )

        putCascade(
            "datalist", listOf(
                CssAttribute("display", "block"),
                CssAttribute("display", "none")
            )
        )

        putCascade(
            "dd", listOf(
                CssAttribute("display", "block"),
                CssAttribute("margin-left", "40px")
            )
        )

        putCascade(
            "del", listOf(
                CssAttribute("text-decoration", "line-through")
            )
        )

        putCascade(
            "details", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "dfn", listOf(
                CssAttribute("font-style", "italic")
            )
        )

        putCascade(
            "div", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "dl", listOf(
                CssAttribute("display", "block"),
                CssAttribute("margin-top", "1em"),
                CssAttribute("margin-bottom", "1em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0")
            )
        )

        putCascade(
            "dt", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "em", listOf(
                CssAttribute("font-style", "italic")
            )
        )

        putCascade(
            "fieldset", listOf(
                CssAttribute("display", "block"),
                CssAttribute("margin-left", "2px"),
                CssAttribute("margin-right", "2px"),
                CssAttribute("padding-top", "0.35em"),
                CssAttribute("padding-bottom", "0.625em"),
                CssAttribute("padding-left", "0.75em"),
                CssAttribute("padding-right", "0.75em"),
                CssAttribute("border", "2px groove $internalColor")
            )
        )

        putCascade(
            "figcaption", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "figure", listOf(
                CssAttribute("display", "block"),
                CssAttribute("margin-top", "1em"),
                CssAttribute("margin-bottom", "1em"),
                CssAttribute("margin-left", "40px"),
                CssAttribute("margin-right", "40px")
            )
        )

        putCascade(
            "footer", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "form", listOf(
                CssAttribute("display", "block"),
                CssAttribute("margin-top", "0em")
            )
        )

        putCascade(
            "h1", listOf(
                CssAttribute("display", "block"),
                CssAttribute("font-size", "2em"),
                CssAttribute("margin-top", "0.67em"),
                CssAttribute("margin-bottom", "0.67em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0"),
                CssAttribute("font-weight", "bold")
            )
        )

        putCascade(
            "h2", listOf(
                CssAttribute("display", "block"),
                CssAttribute("font-size", "1.5em"),
                CssAttribute("margin-top", "0.83em"),
                CssAttribute("margin-bottom", "0.83em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0"),
                CssAttribute("font-weight", "bold")
            )
        )

        putCascade(
            "h3", listOf(
                CssAttribute("display", "block"),
                CssAttribute("font-size", "1.17em"),
                CssAttribute("margin-top", "1em"),
                CssAttribute("margin-bottom", "1em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0"),
                CssAttribute("font-weight", "bold")
            )
        )

        putCascade(
            "h4", listOf(
                CssAttribute("display", "block"),
                CssAttribute("margin-top", "1.33em"),
                CssAttribute("margin-bottom", "1.33em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0"),
                CssAttribute("font-weight", "bold")
            )
        )

        putCascade(
            "h5", listOf(
                CssAttribute("display", "block"),
                CssAttribute("font-size", ".83em"),
                CssAttribute("margin-top", "1.67em"),
                CssAttribute("margin-bottom", "1.67em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0"),
                CssAttribute("font-weight", "bold")
            )
        )

        putCascade(
            "h6", listOf(
                CssAttribute("display", "block"),
                CssAttribute("font-size", ".67em"),
                CssAttribute("margin-top", "2.33em"),
                CssAttribute("margin-bottom", "2.33em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0"),
                CssAttribute("font-weight", "bold")
            )
        )

        putCascade(
            "head", listOf(
                CssAttribute("display", "none")
            )
        )

        putCascade(
            "header", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "hr", listOf(
                CssAttribute("display", "block"),
                CssAttribute("margin-top", "0.5em"),
                CssAttribute("margin-bottom", "0.5em"),
                CssAttribute("margin-left", "auto"),
                CssAttribute("margin-right", "auto"),
                CssAttribute("border-style", "inset"),
                CssAttribute("border-width", "1px")
            )
        )

        putCascade(
            "html", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "i", listOf(
                CssAttribute("font-style", "italic")
            )
        )

        putCascade(
            "img", listOf(
                CssAttribute("display", "inline-block")
            )
        )

        putCascade(
            "ins", listOf(
                CssAttribute("text-decoration", "underline")
            )
        )

        putCascade(
            "kbd", listOf(
                CssAttribute("font-family", "monospace")
            )
        )

        putCascade(
            "label", listOf(
                CssAttribute("cursor", "default")
            )
        )

        putCascade(
            "legend", listOf(
                CssAttribute("display", "block"),
                CssAttribute("padding-left", "2px"),
                CssAttribute("padding-right", "2px"),
                CssAttribute("border", "none")
            )
        )

        putCascade(
            "li", listOf(
                CssAttribute("display", "list-item")
            )
        )

        putCascade(
            "link", listOf(
                CssAttribute("display", "none")
            )
        )

        putCascade(
            "map", listOf(
                CssAttribute("display", "inline")
            )
        )

        putCascade(
            "mark", listOf(
                CssAttribute("background-color", "yellow"),
                CssAttribute("color", "black")
            )
        )

        putCascade(
            "menu", listOf(
                CssAttribute("display", "block"),
                CssAttribute("list-style-type", "disc"),
                CssAttribute("margin-top", "1em"),
                CssAttribute("margin-bottom", "1em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0"),
                CssAttribute("padding-left", "40px")
            )
        )

        putCascade(
            "nav", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "ol", listOf(
                CssAttribute("display", "block"),
                CssAttribute("list-style-type", "decimal"),
                CssAttribute("margin-top", "1em"),
                CssAttribute("margin-bottom", "1em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0"),
                CssAttribute("padding-left", "40px")
            )
        )

        putCascade(
            "output", listOf(
                CssAttribute("display", "inline")
            )
        )

        putCascade(
            "p", listOf(
                CssAttribute("display", "block"),
                CssAttribute("margin-top", "1em"),
                CssAttribute("margin-bottom", "1em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0")
            )
        )

        putCascade(
            "param", listOf(
                CssAttribute("display", "none")
            )
        )

        putCascade(
            "pre", listOf(
                CssAttribute("display", "block"),
                CssAttribute("font-family", "monospace"),
                CssAttribute("white-space", "pre"),
                CssAttribute("margin", "1em 0")
            )
        )

        putCascade(
            "q", listOf(
                CssAttribute("display", "inline")
            )
        )

        putCascade(
            "q::before", listOf(
                CssAttribute("content", "open-quote")
            )
        )

        putCascade(
            "q::after", listOf(
                CssAttribute("content", "close-quote")
            )
        )

        putCascade(
            "rt", listOf(
                CssAttribute("line-height", "normal")
            )
        )

        putCascade(
            "s", listOf(
                CssAttribute("text-decoration", "line-through")
            )
        )

        putCascade(
            "samp", listOf(
                CssAttribute("font-family", "monospace")
            )
        )

        putCascade(
            "script", listOf(
                CssAttribute("display", "none")
            )
        )

        putCascade(
            "section", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "small", listOf(
                CssAttribute("font-size", "smaller")
            )
        )

        putCascade(
            "strike", listOf(
                CssAttribute("text-decoration", "line-through")
            )
        )

        putCascade(
            "strong", listOf(
                CssAttribute("font-weight", "bold")
            )
        )

        putCascade(
            "style", listOf(
                CssAttribute("display", "none")
            )
        )

        putCascade(
            "sub", listOf(
                CssAttribute("vertical-align", "sub"),
                CssAttribute("font-size", "smaller")
            )
        )

        putCascade(
            "summary", listOf(
                CssAttribute("display", "block")
            )
        )

        putCascade(
            "sup", listOf(
                CssAttribute("vertical-align", "super"),
                CssAttribute("font-size", "smaller")
            )
        )

        putCascade(
            "table", listOf(
                CssAttribute("display", "table"),
                CssAttribute("border-collapse", "separate"),
                CssAttribute("border-spacing", "2px"),
                CssAttribute("border-color", "gray")
            )
        )

        putCascade(
            "tbody", listOf(
                CssAttribute("display", "table-row-group"),
                CssAttribute("vertical-align", "middle"),
                CssAttribute("border-color", "inherit")
            )
        )

        putCascade(
            "td", listOf(
                CssAttribute("display", "table-cell"),
                CssAttribute("vertical-align", "inherit")
            )
        )

        putCascade(
            "tfoot", listOf(
                CssAttribute("display", "table-footer-group"),
                CssAttribute("vertical-align", "middle"),
                CssAttribute("border-color", "inherit")
            )
        )

        putCascade(
            "th", listOf(
                CssAttribute("display", "table-cell"),
                CssAttribute("vertical-align", "inherit"),
                CssAttribute("font-weight", "bold"),
                CssAttribute("text-align", "center")
            )
        )

        putCascade(
            "thead", listOf(
                CssAttribute("display", "table-header-group"),
                CssAttribute("vertical-align", "middle"),
                CssAttribute("border-color", "inherit")
            )
        )

        putCascade(
            "title", listOf(
                CssAttribute("display", "none")
            )
        )

        putCascade(
            "tr", listOf(
                CssAttribute("display", "table-row"),
                CssAttribute("vertical-align", "inherit"),
                CssAttribute("border-color", "inherit")
            )
        )

        putCascade(
            "u", listOf(
                CssAttribute("text-decoration", "underline")
            )
        )

        putCascade(
            "ul", listOf(
                CssAttribute("display", "block"),
                CssAttribute("list-style-type", "disc"),
                CssAttribute("margin-top", "1em"),
                CssAttribute("margin-bottom", "1 em"),
                CssAttribute("margin-left", "0"),
                CssAttribute("margin-right", "0"),
                CssAttribute("padding-left", "40px")
            )
        )

        putCascade(
            "var", listOf(
                CssAttribute("font-style", "italic")
            )
        )
    }

    fun getCascade(tag: String?): CssCascade? = cascades[tag]

    fun getCascadeOrBlock(tag: String): CssCascade {
        putCascade(tag, defaultBlockAttributes)
        return cascades.getValue(tag)
    }

    private fun putCascade(tag: String, attrs: List<CssAttribute>) {
        cascades[tag] = CssCascade(
            listOf(createTagSelector(tag)),
            attrs
        )
    }

    private fun createTagSelector(tag: String) = CssSelector().also {
        it.entries.add(CssSelectorEntry().also {
            it.withNodeTag = tag
        })
    }
}