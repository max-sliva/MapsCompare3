import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.ButtonBar
import javafx.scene.control.ComboBox
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter

class MainWindow {
    @FXML lateinit var mainPane: BorderPane
    @FXML lateinit var stackPaneWithImages: StackPane
    @FXML lateinit var imagePicker: ComboBox<String>
    @FXML lateinit var buttonBox: ButtonBar
    val fileToImageViewMap: HashMap<String, ImageView> = HashMap()
    fun onAddImageClick(actionEvent: ActionEvent) {
        println("Add image")
        println(mainPane.scene.window)
        val fileChooser = FileChooser()
        fileChooser.title = "Open Image File"
        fileChooser.extensionFilters.addAll(
            ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
            ExtensionFilter("All Files", "*.*")
        )
        val file = fileChooser.showOpenDialog(mainPane.scene.window)

        if (file!=null) {
            println("file = ${file.path}")
            val imageView = ImageView()
//            val image =
            imageView.image = Image("file:${file.path}")
            imageView.isPreserveRatio = true
            imageView.fitWidthProperty().bind(mainPane.widthProperty())
//            imageView.fitHeightProperty().bind(mainPane.heightProperty())
            stackPaneWithImages.children.add(imageView)
            imagePicker.items.add(file.name)
            fileToImageViewMap[file.name] = imageView
        }
        imagePicker.onAction = EventHandler {
            val comboBox = it.source as ComboBox<String>
            println("image pick = ${comboBox.value}")
            buttonBox.isDisable = false
        }
    }

    fun deleteCurImage(actionEvent: ActionEvent) {
        stackPaneWithImages.children.remove(fileToImageViewMap[imagePicker.value])
        imagePicker.items.remove(imagePicker.value)
        if (imagePicker.items.isEmpty())  buttonBox.isDisable = true
    }
}
