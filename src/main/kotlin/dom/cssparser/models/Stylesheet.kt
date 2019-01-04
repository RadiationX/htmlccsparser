package dom.cssparser.models

import dom.format
import kotlin.math.max

class Stylesheet {

    var mainTime = 0L
    var attrTime = 0L
    var selectorTime = 0L
    val allTime
        get() = mainTime + selectorTime + attrTime

    val cascades = mutableListOf<CssCascade>()
    val selectors = mutableMapOf<Int, MutableList<CssSelector>>()
    val selectorEntries = mutableMapOf<Int, MutableList<CssSelectorEntry>>()

    fun putCascades(cascades: List<CssCascade>) {
        this.cascades.addAll(cascades)
        cascades.forEach { cascade ->
            cascade.selectors.forEach { selector ->
                selector.entries.forEach { entry ->
                    putSelectorEntry(entry)
                }
                putSelector(selector)
            }
        }
    }

    fun putSelector(selector: CssSelector) {
        val key = selector.hashCode()
        val addedSelectors = selectors[key] ?: (mutableListOf<CssSelector>()).also {
            selectors[key] = it
        }
        addedSelectors.add(selector)
    }

    fun putSelectorEntry(entry: CssSelectorEntry) {
        val key = entry.hashCode()
        val addedEntries = selectorEntries[key] ?: (mutableListOf<CssSelectorEntry>()).also {
            selectorEntries[key] = it
        }
        addedEntries.add(entry)
    }

    fun getStatisticInfo(): String {
        val allTimeMillis = allTime / 1000000.0
        val mainTimeMillis = mainTime / 1000000.0
        val selectorTimeMillis = selectorTime / 1000000.0
        val attrTimeMillis = attrTime / 1000000.0


        val allF = max(allTimeMillis, 1.0)
        return "time: all=${allTimeMillis.format(2)}, style=${mainTimeMillis.format(2)}ms(${(mainTimeMillis / allF).format(2)}%), selector=${selectorTimeMillis.format(2)}ms(${(selectorTimeMillis / allF).format(2)}%), body=${attrTimeMillis.format(2)}ms(${(attrTimeMillis / allF).format(2)}%)"
    }
}