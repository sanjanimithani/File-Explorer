import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.*
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.scene.control.ListView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.stage.DirectoryChooser
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import kotlin.io.path.Path

class Main : Application() {

    override fun start(primaryStage: Stage?) {

        // Initializing variables and values
        val homeDir = "${System.getProperty("user.dir")}/test/"
        var dirString = "${System.getProperty("user.dir")}/test/"
        var dir = File(dirString)
        val filesListView = ListView<String>()
        val statusBar = Label()

        // Dialogs
        val renameAlert = TextInputDialog()
        renameAlert.title = "Rename File"
        renameAlert.contentText = "New Filename:"
        renameAlert.headerText = "Please enter a new filename below."

        val errorAlert = Alert(AlertType.ERROR)
        errorAlert.contentText = "Invalid"
        errorAlert.headerText = "Error"

        val deleteAlert = Alert(AlertType.CONFIRMATION)
        deleteAlert.headerText = "Please confirm you would like to proceed with deleting."

        val moveAlert = Alert(AlertType.CONFIRMATION)
        moveAlert.headerText = "Move Action"

        val fileDialog = DirectoryChooser()

        // Menu bar initialization
        val homeAction = MenuItem("Home")
        val prevAction = MenuItem("Prev")
        val nextAction = MenuItem("Next")
        val renameAction = MenuItem("Rename")
        val moveAction = MenuItem("Move")
        val deleteAction = MenuItem("Delete")

        // Toolbar setup and initialization
        val homeButton = Button("Home", ImageView("home.png"))
        val prevButton = Button("Prev", ImageView("prev.png"))
        val nextButton = Button("Next", ImageView("right.png"))
        val deleteButton = Button("Delete", ImageView("delete.png"))
        val renameButton = Button("Rename", ImageView("rename.png"))
        val moveButton = Button("Move", ImageView("move.png"))

        // Disabling/Enabling buttons and menu options
        fun disableHome() {
            homeButton.isDisable = true
            prevButton.isDisable = true
            homeAction.isDisable = true
            prevAction.isDisable = true
        }

        fun enableHome() {
            homeButton.isDisable = false
            prevButton.isDisable = false
            homeAction.isDisable = false
            prevAction.isDisable = false
        }

        fun enableNextButton(){
            nextButton.isDisable = false
            nextAction.isDisable = false
        }

        fun disableNextButton() {
            nextButton.isDisable = true
            nextAction.isDisable = true
        }

        // Centre Pane of Window
        val centrePane = HBox().apply {
            prefWidth = 100.0
            alignment = Pos.CENTER
            background = Background(BackgroundFill(Color.valueOf("#FFFFFF"), null, null))
        }

        // Setting files viewer
        fun setListView() {
            val newFiles = FilesReader(dir).sortedFiles
            filesListView.items.clear()
            filesListView.items = FXCollections.observableArrayList(newFiles)
            filesListView.selectionModel.select(0)
            filesListView.selectionModel.selectionMode = SelectionMode.SINGLE
        }

        // Setting status bar
        fun setStatusBar(filename: String?) {
            filename ?: return
            if (dirString == homeDir) {
                disableHome()
            } else {
                enableHome()
            }
            statusBar.text = dirString.replace("\\", "/") + filename
        }

        // Setting preview pane
        fun setPreviewPane(file: String?) {

            centrePane.children.clear()
            disableNextButton()

            file ?: return

            println("IN PREVIEW PANE")
            val path = dirString + file
            println(path)
            val curr = File(dirString + file)
            val ext = curr.extension
            println(ext)

            //Text File
            if ((ext == "txt") or (ext == "md")) {
                val textFile = TextFileReader(curr).fileText
                val textView = TextArea()
                textView.maxWidth = centrePane.width
                textView.isWrapText = true
                textView.scrollLeft = 100.0
                textView.text = textFile
                centrePane.children.add(textView)
            }

            //Image
            else if ((ext == "png") or (ext == "jpg") or (ext == "bmp")) {
                val image = Image(FileInputStream(path))
                val imageViewer = ImageView(image)
                imageViewer.fitWidth = centrePane.width
                imageViewer.isPreserveRatio = true
                centrePane.children.add(imageViewer)
            }

            //Unknown Type
            else if (ext != "") {
                val label = Label()
                label.text = "Unsupported File Type"
                centrePane.children.add(label)
            }

            else {
                enableNextButton()
            }
        }

        // Navigating into directory
        fun navigateDown() {
            val dirName = filesListView.selectionModel.selectedItem
            val path = dirString + dirName
            dir = File(path)
            if (dir.extension != "") {
                return
            }
            dirString = path
            setListView()
            setPreviewPane(filesListView.selectionModel.selectedItem)
            setStatusBar(filesListView.selectionModel.selectedItem)
        }

        // Navigating up from a directory
        fun navigateUp() {
            if (dir.name != "test") {
                dirString = dir.parentFile.absolutePath + "/"
                dir = File(dirString)
                println(dirString)
                setListView()
                setPreviewPane(filesListView.selectionModel.selectedItem)
                setStatusBar(filesListView.selectionModel.selectedItem)
            }
        }

        // Return to home directory = test
        fun resetToHome() {
            dirString = homeDir
            dir = File(dirString)
            setListView()
            setStatusBar(filesListView.items[0])
            setPreviewPane(filesListView.items[0])
        }

        // Delete file or directory
        fun deleteFile() {
            deleteAlert.showAndWait()
            if (deleteAlert.result == ButtonType.OK) {
                val filename = filesListView.selectionModel.selectedItem
                val filepath = dirString + filename
                val file = File(filepath)
                if (file.extension == "") {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
                setListView()
                setStatusBar(filesListView.selectionModel.selectedItem)
                setPreviewPane(filesListView.selectionModel.selectedItem)
            }
        }

        // Display error dialog with custom message
        fun displayError(errorMessage: String) {
            errorAlert.contentText = errorMessage
            errorAlert.showAndWait()
        }

        // Rename file or directory
        fun renameFile() {
            val result = renameAlert.showAndWait()
            if (result.isPresent) {
                val currFilename = filesListView.selectionModel.selectedItem
                val newFilename = renameAlert.editor.text
                val filepath = dirString + currFilename
                val file = File(filepath)
                if ((newFilename == "") or (newFilename.indexOf("*") > 0) or (newFilename.indexOf("/") > 0)) {
                        displayError("Invalid Filename. Please Try Again.")
                } else {
                    file.renameTo(File(dirString + newFilename))
                    setListView()
                    setStatusBar(filesListView.items[0])
                    setPreviewPane(filesListView.items[0])
                }
            }
        }

        // Move file or directory
        fun moveFile() {
            val fileDialogResult = fileDialog.showDialog(primaryStage)
            fileDialogResult ?: return
            val currFile = filesListView.selectionModel.selectedItem
            val location = fileDialogResult.toString()

            val currPath = Path(dirString + "/" + currFile)
            val newPath = Path(location + "/" + currFile)
            moveAlert.contentText = "Moving " + currPath.toString() + " to " + newPath.toString()
            val result = moveAlert.showAndWait()
            if (result.get() == ButtonType.OK) {
                try {
                    Files.move(currPath, newPath)
                    setListView()
                    setStatusBar(filesListView.items[0])
                } catch (e: Exception) {
                    displayError( "Destination is invalid.")
                }
            }
        }

        //Initializing all elements in window
        setListView()
        setStatusBar(filesListView.items[0])
        setPreviewPane(filesListView.items[0])

        // Create panels
        val bottomPane = statusBar.apply {
        }

        val leftPane = filesListView.apply {
            selectionModel.selectionMode = SelectionMode.SINGLE
            setOnMouseClicked { event ->
                if (event.clickCount == 2) {
                   navigateDown()
                } else {
                    setPreviewPane(filesListView.selectionModel.selectedItem)
                    setStatusBar(filesListView.selectionModel.selectedItem)
                }
            }
            setOnKeyPressed { event ->
                if (event.code == KeyCode.DOWN || event.code == KeyCode.UP) {
                    setPreviewPane(filesListView.selectionModel.selectedItem)
                    setStatusBar(filesListView.selectionModel.selectedItem)
                } else if (event.code == KeyCode.ENTER) {
                    navigateDown()
                } else if (event.code == KeyCode.BACK_SPACE || event.code == KeyCode.DELETE) {
                    navigateUp()
                }
            }
        }

        val topPane = VBox().apply {
            prefHeight = 30.0
            background = Background(BackgroundFill(Color.valueOf("#00ffff"), null, null))
            setOnMouseClicked { println("top pane clicked") }

            children.addAll(
                    MenuBar().apply {
                        menus.add(Menu("File").apply{
                            items.add(homeAction.apply {
                                setOnAction { resetToHome() }
                            })
                            items.add(prevAction.apply{
                                setOnAction { navigateUp() }
                            })
                            items.add(nextAction.apply {
                                setOnAction { navigateDown() }
                            })
                        })
                        menus.add(Menu("Actions").apply{
                            items.add(renameAction.apply{
                                setOnAction { renameFile() }
                            })
                            items.add(moveAction.apply {
                                setOnAction { moveFile() }
                            })
                            items.add(deleteAction.apply {
                                setOnAction { deleteFile() }
                            })
                        })

                    },
                    ToolBar().apply {
                        items.add(homeButton.apply{
                            setOnMouseClicked {
                                resetToHome()
                            }
                        })
                        items.add(prevButton.apply{
                            setOnMouseClicked {
                                navigateUp()
                            }
                        })
                        items.add(nextButton.apply{
                            setOnMouseClicked {
                                navigateDown()
                            }
                        })
                        items.add(renameButton.apply{
                            setOnMouseClicked {
                                renameFile()
                            }
                        })
                        items.add(moveButton.apply{
                            setOnMouseClicked {
                                moveFile()
                            }
                        })
                        items.add(deleteButton.apply{
                            setOnMouseClicked {
                                deleteFile()
                            }
                        })
                    })
        }

        // put the panels side-by-side in a container
        val root = BorderPane().apply {
            left = leftPane
            center = centrePane
            top = topPane
            bottom = bottomPane
        }

        // create the scene and show the stage
        with (primaryStage!!) {
            scene = Scene(root, 600.0, 400.0)
            title = "File Browser"
            show()
        }
    }
}


