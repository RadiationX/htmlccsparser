package dom.cssparser.models

class CssSelector {

    @JvmField
    var cascade: CssCascade? = null
    val entries = mutableListOf<CssSelectorEntry>()

    fun addEntry(entry: CssSelectorEntry) {
        entry.parent = this
        entries.add(entry)
    }

    fun getData(): String {
        /*entries.forEach {
            println(it.getSelectorEntryDetail())
        }*/
        return entries.joinToString(" ") { it.getSelectorEntry() }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        (other as? CssSelector)?.also { otherSelector ->
            if (entries.size != otherSelector.entries.size) {
                return false
            }
            if (!(0 until entries.size).all { (entries[it] == otherSelector.entries[it]) }) {
                return false
            }
            return true
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return entries.hashCode()
    }


}