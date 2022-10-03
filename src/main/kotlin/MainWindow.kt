import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser

class MainWindow {
    @FXML lateinit var mainPane: BorderPane
    @FXML lateinit var stackPaneWithImages: StackPane
    @FXML lateinit var imagePicker: ComboBox<String>
    fun onAddImageClick(actionEvent: ActionEvent) {
        println("Add image")
        println(mainPane.scene.window)
        val fileChooser = FileChooser()
        fileChooser.setTitle("Open Resource File")
        val file = fileChooser.showOpenDialog(mainPane.scene.window)
        println("file = ${file.path}")
        if (file!=null) {
            val imageView = ImageView()
//            val image =
            imageView.image = Image("file:${file.path}")
            stackPaneWithImages.children.add(imageView)
            imagePicker.items.add(file.name)
        }
    }
}
