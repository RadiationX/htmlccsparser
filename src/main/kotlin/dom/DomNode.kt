package dom

import dom.cssparser.models.CssAttribute
import dom.cssparser.models.CssCascade
import dom.htmlparser.HtmlNode

class DomNode(
    val htmlNode: HtmlNode
) {
    var defaultCascade: CssCascade? = null
    val cascades = mutableListOf<CssCascade>()
    val activeCssAttributes = mutableListOf<CssAttribute>()

    init {
        defaultCascade = DefaultCssCascade.getCascade(htmlNode.name)
        defaultCascade?.also {
            cascades.add(it)
        }
        updateActiveAttributes()
    }

    fun putCascades(cascades: List<CssCascade>) {
        this.cascades.addAll(cascades)
        updateActiveAttributes()
    }

    private fun updateActiveAttributes() {
        activeCssAttributes.clear()
        cascades.forEach { cascade ->
            cascade.attributes.forEach { newAttr ->
                val oldAttr = activeCssAttributes.find { it.name == newAttr.name }

                if (oldAttr == null) {
                    activeCssAttributes.add(newAttr)
                } else {
                    val newImportant = newAttr.important
                    val oldImportant = oldAttr.important
                    if (!oldImportant || newImportant) {
                        activeCssAttributes.remove(oldAttr)
                        activeCssAttributes.add(newAttr)
                    }
                }
            }
        }
    }
}