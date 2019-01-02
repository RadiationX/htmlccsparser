import java.io.File

object Utils {
    fun loadFile(file: String): String {
        return File(Thread.currentThread().contextClassLoader.getResource(file).file).readText()
    }
}