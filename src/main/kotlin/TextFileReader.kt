import java.io.File
import java.lang.Exception
import java.util.Scanner

class TextFileReader (file: File) {
    var fileText: String = ""
    init {
        try {
            val scanner = Scanner(file)

            while (scanner.hasNext()) {
                fileText += scanner.nextLine()
            }
        } catch (e: Exception) {
            fileText = "File Cannot Be Read."
        }
    }
}