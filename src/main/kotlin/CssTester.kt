import dom.cssparser.CssParserTask
import dom.cssparser.models.Stylesheet

class CssTester {

    init {
        val text = Utils.loadFile("kekolol.css")
        println(Thread.currentThread())
        CssParserTask(text).parse(object : CssParserTask.StylesheetCallback {
            override fun onSuccess(stylesheet: Stylesheet) {
                println(Thread.currentThread())
                println("onSuccess")
                println(stylesheet.getStatisticInfo())
                println("stylesheet ${stylesheet.selectors.size}, ${stylesheet.selectorEntries.size}")
                println("\nselectors")
                stylesheet.selectors.forEach {
                    println("  ${it.key} => ${it.value.size}")
                }
                println("\nentries")
                stylesheet.selectorEntries.forEach {
                    println("  ${it.key} => ${it.value.size}")
                }
                stylesheet.cascades.forEach {
                    println(it.getData())
                }
            }

            override fun onError(throwable: Throwable) {
                println("onError ${throwable.message}")
            }
        })
    }
}