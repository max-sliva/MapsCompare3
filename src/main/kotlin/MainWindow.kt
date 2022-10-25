import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.geometry.Rectangle2D
import javafx.scene.SnapshotParameters
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.ComboBox
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.ArrayList

class MainWindow {
    @FXML lateinit var mainPane: BorderPane //main window
    @FXML lateinit var stackPaneWithImages: StackPane
    @FXML lateinit var imagePicker: ComboBox<String>
    @FXML lateinit var buttonBox: ButtonBar
    @FXML lateinit var opacitySlider: Slider
    @FXML lateinit var buttomSlider: Slider
    @FXML lateinit var gifBtn: Button
    var isMakingGIF = false
    val fileToImageViewMap: HashMap<String, ImageView> = HashMap()
    var dirPath: String = ""
    var imgCount = 0
    val imagesArrayList = ArrayList<WritableImage>()
    fun onAddImageClick(actionEvent: ActionEvent) {
        opacitySlider.value = 100.0
        println("Add image")
        println(mainPane.scene.window)
        val fileChooser = FileChooser().apply{
            title = "Open Image File"
            val currentPath: String = Paths.get(".").toAbsolutePath().normalize().toString()
            initialDirectory = File(currentPath)
            extensionFilters.addAll(ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                                    ExtensionFilter("All Files", "*.*"))
        }
        val file = fileChooser.showOpenDialog(mainPane.scene.window)
        if (file!=null) {
            println("file = ${file.path}")
            val imageView = ImageView()
            imageView.image = Image("file:${file.path}")
            imageView.isPreserveRatio = true
            imageView.scaleX-=0.6
            imageView.scaleY-=0.6

//            imageView.fitHeight = stackPaneWithImages.height
//            imageView.fitWidthProperty().bind(mainPane.widthProperty())
//            imageView.fitHeightProperty().bind(mainPane.heightProperty())
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

        opacitySlider.valueProperty().addListener{ _, oldVal, newVal ->
//            println("slider oldValue = $oldVal")
//            println("slider newValue = $newVal")
            fileToImageViewMap[imagePicker.value]!!.opacity = newVal.toInt() / 100.0
            if (isMakingGIF){
                if (oldVal.toInt()==100) addImageToArrayList()
                if (newVal.toInt()%5==0) addImageToArrayList()
//                if (oldVal.toInt()==100) makeImage("png")
//                if (newVal.toInt()%2==0) makeImage("png")
            }
        }

        buttomSlider.valueProperty().addListener { _, oldVal, newVal ->  //делаем видимой часть картинки в зависимости от положения нижнего бегунка
            val curX = (fileToImageViewMap[imagePicker.value]!!.image.width * newVal.toInt()) / 100
            fileToImageViewMap[imagePicker.value]!!.viewport = Rectangle2D(curX, 0.0, fileToImageViewMap[imagePicker.value]!!.image.width,
                                                                                                        fileToImageViewMap[imagePicker.value]!!.image.height)
            fileToImageViewMap[imagePicker.value]!!.translateX = curX * fileToImageViewMap[imagePicker.value]!!.scaleX //и сдвигаем картинку на нужное значение
            if (isMakingGIF) {
                if (oldVal.toInt() == 100) addImageToArrayList()
                if (newVal.toInt() % 5 == 0) addImageToArrayList()
            }
//            println("butSlider = $newVal")
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

    fun makeImage(type: String) { //todo make thread for this work
        val snapshot: WritableImage = stackPaneWithImages.snapshot(SnapshotParameters(), null)
        val file = File("$dirPath/img$imgCount.$type")
        imgCount++
        ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null),"$type", file)
//        val image = (stackPaneWithImages as ImageView).image
//        image.
    }

    fun addImageToArrayList(){//todo почитать про SnapshotParameters, чтобы сжать изображение
        val snapshot: WritableImage = stackPaneWithImages.snapshot(SnapshotParameters(), null)
        imagesArrayList.add(snapshot)
    }

    fun gifBtnClick(actionEvent: ActionEvent) {
        isMakingGIF = !isMakingGIF
        if (gifBtn.text =="start gif") beginGifWork()
        if (gifBtn.text =="stop gif") {
            val gifWork = GifWork(dirPath)
//            gifWork.makeGif()
            gifWork.makeGifFromArrayList(imagesArrayList)
        }
        gifBtn.text = if (isMakingGIF) "stop gif" else "start gif"
    }

    private fun beginGifWork() {
        println("gif work has begun!")
        imgCount = 0
        imagesArrayList.clear()
        val rightNow = Calendar.getInstance()
        val date = rightNow.time as Date
        val sdf = SimpleDateFormat("dd_MM_yy HH_mm_ss")
        dirPath = sdf.format(date)
//        val currentPath: String = Paths.get(".").toAbsolutePath().normalize().toString()
        println("date = ${sdf.format(date)}")
        Files.createDirectories(Paths.get("$dirPath"))
    }

    fun oneGif(actionEvent: ActionEvent) {
        if (isMakingGIF){
           makeImage("png")
        }

    }
}
