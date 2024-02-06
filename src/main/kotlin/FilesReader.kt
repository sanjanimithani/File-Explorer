import java.io.File

class FilesReader (dir: File) {
    val sortedFiles = mutableListOf<String>()
    init {
        val dirFiles = dir.listFiles()

        if (dirFiles != null) {
            for (file in dirFiles) {
                val name = file.name
                val ext = file.extension

                if (ext != "") {
                    sortedFiles.add(name)
                } else {
                    sortedFiles.add(name + "/")
                }
            }
            sortedFiles.sort()
        }
    }

}
