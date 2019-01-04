package dom.cssparser

import dom.cssparser.models.*
import dom.findAll
import dom.mapOnce
import dom.trimWhiteSpace
import java.util.regex.Pattern

open class CssParserTask(
    private val cssSource: String
) {

    companion object {
        /* Group names */
        private const val STYLESHEET_COMMENT = 1
        private const val STYLESHEET_SELECTOR = 2
        private const val STYLESHEET_ATTRS = 3

        private const val SELECTOR_ELEM_MARKER = 1
        private const val SELECTOR_ELEM_NAME = 2
        private const val SELECTOR_ATTR = 3
        private const val SELECTOR_PSEUDO_MARKER = 4
        private const val SELECTOR_PSEUDO_NAME = 5
        private const val SELECTOR_SPECIFY = 6
        private const val SELECTOR_DELIMITER = 7

        private const val SELECTOR_ATTR_NAME = 1
        private const val SELECTOR_ATTR_TYPE = 2
        private const val SELECTOR_ATTR_VALUE = 4

        private const val ATTR_NAME = 1
        private const val ATTR_VALUE = 2
        private const val ATTR_MARKER_IMPORTANT = 3

        private const val ELEM_MARKER_CLASS = "."
        private const val ELEM_MARKER_ID = "#"
        private const val ELEM_NAME_ALL = "*"
        private const val SPECIFY_INSIDE = ">"
        private const val SPECIFY_NEXT = "+"
        private const val SPECIFY_ALL_NEXT = "~"
        private const val DELIMITER_SELECTOR = ","
        private const val DELIMITER_ENTRY = " "

        private val ATTR_EQUALS = ""
        private val ATTR_CONTAINS = "*"
        private val ATTR_CONTAINS_MULTIPLE = "~"
        private val ATTR_START_WITH = "^"
        private val ATTR_START_MULTIPLE = "|"
        private val ATTR_END_WITH = "$"

        /* Patterns */
        private const val stylesheet =
            "(\\/\\*[^*]*?\\*\\/)|(?<=\\*\\/|\\A|\\})[\\s]*([\\.\\w\\d\\>\\s\\(\\)\\:\\#\\*\\+\\~\\,\\[\\]\\\$\\=\\|\\-\\\"\\'\\^]+)\\{([^\\}]+)?\\}"
        private const val selector =
            "([\\.\\#])?([\\w\\-\\*]+)|\\[([^\\]]+)\\]|(::?)((?:[\\w\\-]+\\([^\\)]+\\))|[\\w\\-]+)|([\\>\\~\\+])|([, ])"
        private const val selectorAttr = "^([\\w\\-]+)(?:([\\~\\|\\^\\\$\\*]?)=(['\"])?([\\w\\-]+)(\\3)?\$)?"
        private const val attribute = "([\\w-]+):([^;]*(\\!important)?[^;]*);"

        private val styleSheetPattern = Pattern.compile(stylesheet)
        private val selectorPattern = Pattern.compile(selector)
        private val selectorAttrPattern = Pattern.compile(selectorAttr)
        private val attributePattern = Pattern.compile(attribute)

        /* Default */
        private var defaultMainCallable: MainCallable = object :
            MainCallable {
            override fun apply(source: String, executor: MainExecutor, callback: MainCallback) {
                try {
                    callback.apply(executor.run(source))
                } catch (ex: Throwable) {
                    callback.onError(ex)
                }
            }
        }

        private var defaultCascadesCallable: CascadesCallable = object :
            CascadesCallable {
            override fun apply(source: List<ParserCascade>, executor: CascadesExecutor, callback: CascadesCallback) {
                try {
                    callback.apply(source.map { executor.run(it) })
                } catch (ex: Throwable) {
                    callback.onError(ex)
                }
            }
        }
    }

    private val mainExecutor = object : MainExecutor {
        override fun run(source: String): List<ParserCascade> {
            return parseStylesheet(source)
        }
    }

    private val cascadesExecutor = object : CascadesExecutor {
        override fun run(parserCascade: ParserCascade): CssCascade {
            val selectors = parseSelector(parserCascade.selectorSrc)
            val attrs = parseAttrs(parserCascade.attrSrc)
            return CssCascade(selectors, attrs)
        }
    }

    private val mainCallback = object : MainCallback {
        override fun apply(sourceCascades: List<ParserCascade>) {
            try {
                cascadesCallable.apply(sourceCascades, cascadesExecutor, cascadesCallback)
            } catch (ex: Throwable) {
                onError(ex)
            }
        }

        override fun onError(ex: Throwable) {
            this@CssParserTask.onError(ex)
        }
    }

    private val cascadesCallback = object : CascadesCallback {
        override fun apply(cascades: List<CssCascade>) {
            try {
                val stylesheet = Stylesheet()
                stylesheet.putCascades(cascades)
                this@CssParserTask.onSuccess(stylesheet)
            } catch (ex: Throwable) {
                onError(ex)
            }
        }

        override fun onError(ex: Throwable) {
            this@CssParserTask.onError(ex)
        }
    }

    var mainCallable: MainCallable =
        defaultMainCallable
    var cascadesCallable: CascadesCallable =
        defaultCascadesCallable

    private lateinit var resultCallback: StylesheetCallback

    private var executing = false
    private var isFinal = false

    private var mainTime = 0L
    private var attrTime = 0L
    private var selectorTime = 0L

    fun parse(resultCallback: StylesheetCallback) {
        if (executing || isFinal) {
            if (isFinal) {
                println("Current task has final status")
            }
            return
        }
        executing = true
        this.resultCallback = resultCallback
        try {
            mainCallable.apply(cssSource, mainExecutor, mainCallback)
        } catch (ex: Throwable) {
            onError(ex)
        }
    }

    protected open fun onError(ex: Throwable) {
        executing = false
        isFinal = true
        resultCallback.onError(ex)
    }

    protected open fun onSuccess(stylesheet: Stylesheet) {
        executing = false
        isFinal = true
        stylesheet.mainTime = mainTime
        stylesheet.selectorTime = selectorTime
        stylesheet.attrTime = attrTime
        resultCallback.onSuccess(stylesheet)
    }

    private fun parseStylesheet(cssSource: String): List<ParserCascade> {
        val time = System.currentTimeMillis()
        val matcher = styleSheetPattern.matcher(cssSource)
        val result = mutableListOf<ParserCascade>()
        matcher.findAll {
            val commentSrc = it.group(STYLESHEET_COMMENT)
            if (commentSrc == null) {
                val selectorSrc = it.group(STYLESHEET_SELECTOR)
                val attrsSrc = it.group(STYLESHEET_ATTRS)
                val selector = selectorSrc.trimWhiteSpace()
                val attrs = attrsSrc.orEmpty().trimWhiteSpace()
                result.add(ParserCascade(selector, attrs))
            }
        }
        mainTime += (System.currentTimeMillis() - time)
        return result
    }

    private fun parseSelector(selectorSrc: String): List<CssSelector> {
        val time = System.currentTimeMillis()
        val matcherSrc = selectorPattern.matcher(selectorSrc)

        var currentEntry = CssSelectorEntry()
        var currentSelector = CssSelector()
        currentSelector.addEntry(currentEntry)
        val selectors = mutableListOf<CssSelector>()
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
                currentSelector = CssSelector()
                selectors.add(currentSelector)
            }

            if (isSelectorDelimiter || !lastIsNew && isNewEntry) {
                currentEntry = CssSelectorEntry()
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
                currentEntry.addAttr(parseSelectorAttr(it))
            }

            pseudoName?.also {
                currentEntry.addPseudo(it)
            }

            specify?.also {
                currentEntry.withSpecify = when (it) {
                    SPECIFY_INSIDE -> CssSpecify.INSIDE
                    SPECIFY_NEXT -> CssSpecify.NEXT
                    SPECIFY_ALL_NEXT -> CssSpecify.ALL_NEXT
                    else -> CssSpecify.DEFAULT
                }
            }
        }


        val currentTime = (System.currentTimeMillis() - time)
        selectorTime += currentTime
        return selectors
    }

    private fun parseSelectorAttr(attrsSrc: String): CssSelectorAttribute {
        val result = selectorAttrPattern.matcher(attrsSrc)
            .mapOnce {
                val nameSrc = it.group(SELECTOR_ATTR_NAME)
                val typeSrc = it.group(SELECTOR_ATTR_TYPE)
                val valueSrc = it.group(SELECTOR_ATTR_VALUE)
                val type = when (typeSrc) {
                    ATTR_EQUALS -> CssSelectorAttribute.Type.EQUALS
                    ATTR_CONTAINS -> CssSelectorAttribute.Type.CONTAINS
                    ATTR_CONTAINS_MULTIPLE -> CssSelectorAttribute.Type.CONTAINS_MULTIPLE
                    ATTR_START_WITH -> CssSelectorAttribute.Type.START_WITH
                    ATTR_START_MULTIPLE -> CssSelectorAttribute.Type.START_MULTIPLE
                    ATTR_END_WITH -> CssSelectorAttribute.Type.END_WITH
                    else -> null
                }
                CssSelectorAttribute(nameSrc, valueSrc, type)
            }
        return result!!
    }

    private fun parseAttrs(attrsSrc: String): List<CssAttribute> {
        val time = System.currentTimeMillis()
        val matcherSrc = attributePattern.matcher(attrsSrc)
        val result = mutableListOf<CssAttribute>()
        matcherSrc.findAll {
            val nameSrc = it.group(ATTR_NAME)
            val valueSrc = it.group(ATTR_VALUE)
            val importantSrc = it.group(ATTR_MARKER_IMPORTANT)

            val name = nameSrc.trimWhiteSpace()
            val value = valueSrc.trimWhiteSpace()
            result.add(CssAttribute(name, value).also {
                it.important = importantSrc != null
            })
        }
        attrTime += (System.currentTimeMillis() - time)
        return result
    }

    interface MainCallable {
        fun apply(source: String, executor: MainExecutor, callback: MainCallback)
    }

    interface MainExecutor {
        fun run(source: String): List<ParserCascade>
    }

    interface MainCallback {
        fun apply(sourceCascades: List<ParserCascade>)
        fun onError(ex: Throwable)
    }

    interface CascadesCallable {
        fun apply(source: List<ParserCascade>, executor: CascadesExecutor, callback: CascadesCallback)
    }

    interface CascadesExecutor {
        fun run(parserCascade: ParserCascade): CssCascade
    }

    interface CascadesCallback {
        fun apply(cascades: List<CssCascade>)
        fun onError(ex: Throwable)
    }

    interface StylesheetCallback {
        fun onSuccess(stylesheet: Stylesheet)
        fun onError(throwable: Throwable)
    }

}