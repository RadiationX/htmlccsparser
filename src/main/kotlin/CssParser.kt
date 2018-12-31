import java.util.regex.Pattern

class CssParser {

    companion object {
        private const val stylesheet =
            "(\\/\\*[^*]*?\\*\\/)|(?<=\\*\\/|\\A|\\})[\\s]*?([\\.\\w\\d\\>\\s\\(\\)\\:\\#\\*\\+\\~\\,\\[\\]\\\$\\=\\|]+)\\{([^\\}]+)\\}"
        private const val selector = "(\\.|#|)(\\w+|\\*)|\\[([^\\]]*?)\\]|(::?)([\\w-\\d\\(\\)]*)|(>|~|\\+)|(,| )"
        private const val attribute = "([\\w-]*?):([^;]*);"

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
        private const val ELEM_MARKER_TAG = ""
        private const val ELEM_NAME_ALL = "*"
        private const val SPECIFY_INSIDE = ">"
        private const val SPECIFY_NEXT = "+"
        private const val SPECIFY_ALL_NEXT = "~"
        private const val DELIMITER_SELECTOR = ","
        private const val DELIMITER_ENTRY = " "

        private const val ATTR_NAME = 1
        private const val ATTR_VALUE = 2

    }

    init {
        println("Hello from parser")
        //testStylesheet()
        testSelector()
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
        parseSelector(lel)
    }


    private fun parseStylesheet(cssSource: String): Map<String, String> {
        val matcher = styleSheetPattern.matcher(cssSource)
        val result = mutableMapOf<String, String>()
        matcher.findAll {
            val commentSrc = it.group(STYLESHEET_COMMENT)
            if (commentSrc == null) {
                val selectorSrc = it.group(STYLESHEET_SELECTOR)
                val bodySrc = it.group(STYLESHEET_BODY)
                val selector = selectorSrc.trim()
                val body = bodySrc.trim()
                result[selector] = body
            }
        }
        return result
    }

    private fun parseSelector(selectorSrc: String) {
        val matcherSrc = selectorPattern.matcher(selectorSrc)

        var currentEntry = SelectorEntry()
        var currentSelector = Selector()
        currentSelector.addEntry(currentEntry)
        val selectors = mutableListOf(currentSelector)
        var lastIsNew = true
        matcherSrc.findAll { matcher ->

            val elemMarker = matcher.group(SELECTOR_ELEM_MARKER)
            val elemName = matcher.group(SELECTOR_ELEM_NAME)

            val attr = matcher.group(SELECTOR_ATTR)

            val pseudoMarker = matcher.group(SELECTOR_PSEUDO_MARKER)
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
                ELEM_MARKER_TAG -> {
                    currentEntry.withNodeTag = elemName
                }
                ELEM_MARKER_ID -> {
                    currentEntry.withNodeId = elemName
                }
                ELEM_MARKER_CLASS -> {
                    currentEntry.withClasses.add(elemName)
                }
            }

            attr?.also {
                currentEntry.withAttributes.add(it)
            }

            pseudoName?.also {
                currentEntry.withPseudo.add(it)
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

        println("$selectorSrc")
        println("parsed:")
        selectors.forEach {
            println("res: ${it.getData()}")
        }

    }

}