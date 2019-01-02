import kotlin.math.max

class Stylesheet {

    var mainTime = 0L
    var attrTime = 0L
    var selectorTime = 0L
    val allTime
        get() = mainTime + selectorTime + attrTime

    val cascades = mutableListOf<Cascade>()
    val selectors = mutableMapOf<Int, MutableList<Selector>>()
    val selectorEntries = mutableMapOf<Int, MutableList<SelectorEntry>>()

    fun putCascades(cascades: List<Cascade>) {
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

    fun putSelector(selector: Selector) {
        val key = selector.hashCode()
        val addedSelectors = selectors[key] ?: (mutableListOf<Selector>()).also {
            selectors[key] = it
        }
        addedSelectors.add(selector)
    }

    fun putSelectorEntry(entry: SelectorEntry) {
        val key = entry.hashCode()
        val addedEntries = selectorEntries[key] ?: (mutableListOf<SelectorEntry>()).also {
            selectorEntries[key] = it
        }
        addedEntries.add(entry)
    }

    fun getStatisticInfo(): String {
        val allF = max(allTime.toFloat(), 1f)
        return "time: all=$allTime, style=$mainTime(${mainTime / allF}), selector=$selectorTime(${selectorTime / allF}), body=$attrTime(${attrTime / allF})"
    }
}