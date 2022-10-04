import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.geometry.Rectangle2D
import javafx.scene.control.ButtonBar
import javafx.scene.control.ComboBox
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter

class MainWindow {
    @FXML lateinit var mainPane: BorderPane //main window
    @FXML lateinit var stackPaneWithImages: StackPane
    @FXML lateinit var imagePicker: ComboBox<String>
    @FXML lateinit var buttonBox: ButtonBar
    @FXML lateinit var opacitySlider: Slider
    @FXML lateinit var buttomSlider: Slider
    val fileToImageViewMap: HashMap<String, ImageView> = HashMap()
    fun onAddImageClick(actionEvent: ActionEvent) {
        opacitySlider.value = 100.0
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
            imageView.image = Image("file:${file.path}")
            imageView.isPreserveRatio = true
//            imageView.fitWidthProperty().bind(mainPane.widthProperty())
//            imageView.fitHeightProperty().bind(mainPane.heightProperty())
//            imageView.fitHeight = 100.0;
//            imageView.setFitWidth(100.0);
            stackPaneWithImages.children.add(imageView)
            imagePicker.items.add(file.name)
            fileToImageViewMap[file.name] = imageView
        }
        imagePicker.onAction = EventHandler {
            val comboBox = it.source as ComboBox<String>
            println("image pick = ${comboBox.value}")
            buttonBox.isDisable = false
            opacitySlider.isDisable = false
            buttomSlider.isDisable = false
//            println("imageView = ${fileToImageViewMap[comboBox.value]!!.image}")
            opacitySlider.value = fileToImageViewMap[imagePicker.value]!!.opacity*100
        }

        opacitySlider.valueProperty().addListener{ _, _, newVal ->
            println("slider value = $newVal")
            fileToImageViewMap[imagePicker.value]!!.opacity = newVal.toInt() / 100.0
        }

        buttomSlider.valueProperty().addListener { _, _, newVal ->
            val curX = (fileToImageViewMap[imagePicker.value]!!.image.width * newVal.toInt()) / 100
            fileToImageViewMap[imagePicker.value]!!.viewport = Rectangle2D(curX, 0.0, fileToImageViewMap[imagePicker.value]!!.image.width,
                                                                                                        fileToImageViewMap[imagePicker.value]!!.image.height)
            fileToImageViewMap[imagePicker.value]!!.translateX = curX * fileToImageViewMap[imagePicker.value]!!.scaleX

        }
    }

    fun deleteCurImage(actionEvent: ActionEvent) {
        stackPaneWithImages.children.remove(fileToImageViewMap[imagePicker.value])
        imagePicker.items.remove(imagePicker.value)
        if (imagePicker.items.isEmpty())  buttonBox.isDisable = true
    }

    fun curImageLayerUp(actionEvent: ActionEvent) {
        val i = stackPaneWithImages.children.indexOf(fileToImageViewMap[imagePicker.value])
        stackPaneWithImages.children[i].toFront()
    }

    fun curImageLayerDown(actionEvent: ActionEvent) {
        val i = stackPaneWithImages.children.indexOf(fileToImageViewMap[imagePicker.value])
        stackPaneWithImages.children[i].toBack()
    }

    fun onZoomPlus(actionEvent: ActionEvent) {
        for(imageView in stackPaneWithImages.children){
            imageView.scaleX+=0.2
            imageView.scaleY+=0.2
        }
    }

    fun onZoomMinus(actionEvent: ActionEvent) {
        for(imageView in stackPaneWithImages.children){
            imageView.scaleX-=0.2
            imageView.scaleY-=0.2
        }
    }
}
