import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min

class CssParser {

    companion object {
        private const val stylesheet =
            "(\\/\\*[^*]*?\\*\\/)|(?<=\\*\\/|\\A|\\})[\\s]*([\\.\\w\\d\\>\\s\\(\\)\\:\\#\\*\\+\\~\\,\\[\\]\\\$\\=\\|\\-]+)\\{([^\\}]+)\\}"
        private const val selector =
            "([\\.\\#])?([\\w\\-\\*]+)|\\[([^\\]]+)\\]|(::?)([\\w\\-\\d\\(\\)]+)|([\\>\\~\\+])|([, ])"
        private const val attribute = "([\\w-]+):([^;]*);"

        private val styleSheetPattern = Pattern.compile(stylesheet)
        private val selectorPattern = Pattern.compile(selector)
        private val attributePattern = Pattern.compile(attribute)

        /* Group names */
        private const val STYLESHEET_COMMENT = 1
        private const val STYLESHEET_SELECTOR = 2
        private const val STYLESHEET_BODY = 3

        private const val SELECTOR_ELEM_MARKER = 1
        private const val SELECTOR_ELEM_NAME = 2
        private const val SELECTOR_ATTR = 3
        private const val SELECTOR_PSEUDO_MARKER = 4
        private const val SELECTOR_PSEUDO_NAME = 5
        private const val SELECTOR_SPECIFY = 6
        private const val SELECTOR_DELIMITER = 7

        private const val ELEM_MARKER_CLASS = "."
        private const val ELEM_MARKER_ID = "#"
        private const val ELEM_NAME_ALL = "*"
        private const val SPECIFY_INSIDE = ">"
        private const val SPECIFY_NEXT = "+"
        private const val SPECIFY_ALL_NEXT = "~"
        private const val DELIMITER_SELECTOR = ","
        private const val DELIMITER_ENTRY = " "

        private const val ATTR_NAME = 1
        private const val ATTR_VALUE = 2

    }

    var stylesheetTime = 0L
    var bodyTime = 0L
    var selectorTime = 0L

    val bytesss = testCss1.toByteArray()

    init {
        println("Hello from parser")
        //testStylesheet()
        //testSelector()

        (0 until 10).forEach {
            testall()
        }
    }


    fun testall() {
        stylesheetTime = 0
        selectorTime = 0
        bodyTime = 0
        val styleSheet = parseStylesheet(String(bytesss))
        val cascades = styleSheet.map {
            val selectors = parseSelector(it.key)
            val attrs = parseBody(it.value)
            Cascade(selectors, attrs)
        }
        val allTime = stylesheetTime + selectorTime + bodyTime
        val allF = max(allTime.toFloat(), 1f)
        println("time: all=$allTime, style=$stylesheetTime(${stylesheetTime / allF}), selector=$selectorTime(${selectorTime / allF}), body=$bodyTime(${bodyTime / allF})")

        /*cascades.forEach {
            println(it.getData())
        }*/
    }

    private fun testStylesheet() {
        val styleSheet = parseStylesheet(testCss1)
        println("styleSheet size: ${styleSheet.size}")
        styleSheet.forEach {
            println("selector: ${it.key}; body size: ${it.value.length}")
        }
    }

    private fun testSelector() {
        val lel =
            "[style*=\"color:#000\"],\n" +
                    "[style*=\"color:#000000\"],\n" +
                    "[style*=\"color:black\"] *"
        val selectors = parseSelector(lel)

        println("$lel")
        println("parsed:")
        selectors.forEach {
            println("res: ${it.getData()}")
        }
    }


    private fun parseStylesheet(cssSource: String): Map<String, String> {
        val time = System.currentTimeMillis()
        val matcher = styleSheetPattern.matcher(cssSource)
        val result = mutableMapOf<String, String>()
        matcher.findAll {
            val commentSrc = it.group(STYLESHEET_COMMENT)
            if (commentSrc == null) {
                val selectorSrc = it.group(STYLESHEET_SELECTOR)
                val bodySrc = it.group(STYLESHEET_BODY)
                val selector = selectorSrc.trimWhiteSpace()
                val body = bodySrc.trimWhiteSpace()
                result[selector] = body
            }
        }
        stylesheetTime += (System.currentTimeMillis() - time)
        return result
    }

    private fun parseSelector(selectorSrc: String): List<Selector> {
        val time = System.currentTimeMillis()
        val matcherSrc = selectorPattern.matcher(selectorSrc)

        var currentEntry = SelectorEntry()
        var currentSelector = Selector()
        currentSelector.addEntry(currentEntry)
        val selectors = mutableListOf<Selector>()
        selectors.add(currentSelector)
        var lastIsNew = true

        matcherSrc.findAll { matcher ->

            val elemMarker = matcher.group(SELECTOR_ELEM_MARKER)
            val elemName = matcher.group(SELECTOR_ELEM_NAME)

            val attr = matcher.group(SELECTOR_ATTR)

            //val pseudoMarker = matcher.group(SELECTOR_PSEUDO_MARKER)
            val pseudoName = matcher.group(SELECTOR_PSEUDO_NAME)

            val specify = matcher.group(SELECTOR_SPECIFY)
            val delimiter = matcher.group(SELECTOR_DELIMITER)

            val isSelectorDelimiter = delimiter == DELIMITER_SELECTOR
            val isEntryDelimiter = delimiter == DELIMITER_ENTRY
            val isNewEntry = isEntryDelimiter || specify != null

            if (isSelectorDelimiter) {
                currentSelector = Selector()
                selectors.add(currentSelector)
            }

            if (isSelectorDelimiter || !lastIsNew && isNewEntry) {
                currentEntry = SelectorEntry()
                currentSelector.addEntry(currentEntry)
            }

            lastIsNew = isSelectorDelimiter || isNewEntry

            when (elemMarker) {
                ELEM_MARKER_ID -> {
                    currentEntry.withNodeId = elemName
                }
                ELEM_MARKER_CLASS -> {
                    currentEntry.addClass(elemName)
                }
                else -> {
                    currentEntry.withNodeTag = elemName
                }
            }

            attr?.also {
                currentEntry.addAttr(it)
            }

            pseudoName?.also {
                currentEntry.addPseudo(it)
            }

            specify?.also {
                currentEntry.withSpecify = when (it) {
                    SPECIFY_INSIDE -> Specify.INSIDE
                    SPECIFY_NEXT -> Specify.NEXT
                    SPECIFY_ALL_NEXT -> Specify.ALL_NEXT
                    else -> Specify.DEFAULT
                }
            }
        }


        val currentTime = (System.currentTimeMillis() - time)
        selectorTime += currentTime
        return selectors
    }

    private fun parseBody(bodySrc: String): Map<String, String> {
        val time = System.currentTimeMillis()
        val matcherSrc = attributePattern.matcher(bodySrc)
        val result = mutableMapOf<String, String>()
        matcherSrc.findAll {
            val nameSrc = it.group(ATTR_NAME)
            val valueSrc = it.group(ATTR_VALUE)

            val name = nameSrc.trimWhiteSpace()
            val value = valueSrc.trimWhiteSpace()
            result[name] = value
        }
        bodyTime += (System.currentTimeMillis() - time)
        return result
    }

}