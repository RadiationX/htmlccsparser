class Selector {
    val entries = mutableListOf<SelectorEntry>()

    fun addEntry(entry: SelectorEntry) {
        entry.parent = this
        entries.add(entry)
    }

    fun getData(): String {
        /*entries.forEach {
            println(it.getSelectorEntryDetail())
        }*/
        return entries.joinToString(" ") { it.getSelectorEntry() }
    }
}