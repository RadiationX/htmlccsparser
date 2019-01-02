import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class RxCssParserTask(cssSource: String) : CssParserTask(cssSource) {
    private val disposables = CompositeDisposable()

    init {
        cascadesCallable = object : CssParserTask.CascadesCallable {
            override fun apply(
                source: List<ParserCascade>,
                executor: CssParserTask.CascadesExecutor,
                callback: CssParserTask.CascadesCallback
            ) {
                runCascades(source, executor, callback)
            }
        }
    }

    private fun runCascades(
        source: List<ParserCascade>,
        executor: CssParserTask.CascadesExecutor,
        callback: CssParserTask.CascadesCallback
    ) {
        val disposable = Single
            .fromCallable { source.map { executor.run(it) } }
            .subscribeOn(Schedulers.computation())
            .subscribe({
                callback.apply(it)
            }, {
                callback.onError(it)
            })
        disposables.add(disposable)
    }

    override fun onError(ex: Throwable) {
        super.onError(ex)
        disposables.dispose()
    }

    override fun onSuccess(stylesheet: Stylesheet) {
        super.onSuccess(stylesheet)
        disposables.dispose()
    }
}