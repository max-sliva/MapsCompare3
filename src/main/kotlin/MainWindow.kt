import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.SnapshotParameters
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs

class MainWindow: Initializable {
    @FXML lateinit var mainPane: BorderPane //main window
    @FXML lateinit var paneWithImages: AnchorPane
    @FXML lateinit var imagePicker: ComboBox<String>
    @FXML lateinit var buttonBox: ButtonBar
    @FXML lateinit var opacitySlider: Slider
    @FXML lateinit var bottomSlider: Slider
    @FXML lateinit var gifBtn: Button
    @FXML lateinit var msBtwFrms: TextField
    @FXML lateinit var comboFrameEvery: ComboBox<Int>
    @FXML lateinit var rect : Rectangle
//    @FXML lateinit var anchorPane: AnchorPane
    var isMakingGIF = false
    private val fileToImageViewMap: HashMap<String, ImageView?> = HashMap()
    var dirPath: String = ""
    var imgCount = 0
    private val imagesArrayList = ArrayList<WritableImage>()
    var frameEvery = 5
    private var startX: Double = 0.0
    private var startY: Double = 0.0
    var firstScale = true
//    private lateinit var rect : Rectangle

    fun onAddImageClick(actionEvent: ActionEvent) {
        opacitySlider.value = 100.0
        println("Add image")
        println(mainPane.scene.window)
        val fileChooser = FileChooser().apply{
            title = "Open Image File"
            val currentPath: String = Paths.get(".").toAbsolutePath().normalize().toString()
            initialDirectory = File(currentPath)
            extensionFilters.addAll(ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.tiff", "*.tif"),
                                    ExtensionFilter("All Files", "*.*"))
        }
        val file = fileChooser.showOpenDialog(mainPane.scene.window)
        if (file!=null) {
            println("file = ${file.path}")
            println("extension = ${file.extension}")
            val imageView = ImageView()
            println("imageView.style = ${imageView.style}")
            if (file.extension!="tiff"){
                println("Standard file")
                imageView.image = Image("file:${file.path}")
            } else {
                println("Calling TiffWork")
                val myTiffWork = TiffWork(file)
                val bufImage = myTiffWork.readFileToBufferedImage()
//                val image: Image = SwingFXUtils.toFXImage(bufImage, null)
                imageView.image = SwingFXUtils.toFXImage(bufImage, null)
            }
            imageView.isPreserveRatio = true
//            todo сделать подгон размера картинки под размер окна
//            imageView.scaleX-=0.6
//            imageView.scaleY-=0.6
//            imageView.x = 0.0
//            imageView.y = 0.0
//            val i = arrayOf<Image?>(null)
//            i[0] = imageView.image
            imageView.onMousePressed = EventHandler {
                startX = it.x
                startY = it.y
                paneWithImages.children.remove(rect)
                rect = Rectangle()
                rect.x = startX
                rect.y = startY
                rect.stroke = Color.RED
                rect.fill = Color.TRANSPARENT
                paneWithImages.children.add(rect)
                rect.toFront()
            }

            imageView.setOnMouseDragged { event ->
                val x = event.x
                val y = event.y
//                val wi = WritableImage(i[0]!!.pixelReader, i[0]!!.width.toInt(), i[0]!!.height.toInt())
//                val pw: PixelWriter = wi.pixelWriter
//                pw.setColor(x.toInt(), y.toInt(), Color(1.0, 0.0, 0.0, 1.0))
//
//                i[0] = wi
//                imageView.image = i[0]
                rect.width = abs(x - rect.x)
                rect.height = abs(y - rect.y)
                println("x = ${rect.x}  y = ${rect.y}")
            }
            imageView.onMouseReleased = EventHandler {
                println("Drag ended")
//                val rect = Rectangle2D()
            }
//            imageView.fitHeight = stackPaneWithImages.height
//            imageView.fitWidthProperty().bind(mainPane.widthProperty())
//            imageView.fitHeightProperty().bind(mainPane.heightProperty())
            val cssBordering = """
                    -fx-border-color: green;
                    -fx-border-style: solid;
                    -fx-border-width: 5;
                """.trimIndent()
            paneWithImages.children.add(imageView)
            imagePicker.items.add(file.name)
            fileToImageViewMap[file.name] = imageView

        }
        imagePicker.onAction = EventHandler {
            val comboBox = it.source as ComboBox<String>
            println("image pick = ${comboBox.value}")
            buttonBox.isDisable = false
            opacitySlider.isDisable = false
            bottomSlider.isDisable = false
//            println("imageView = ${fileToImageViewMap[comboBox.value]!!.image}")
            if (imagePicker.value!=null) opacitySlider.value = fileToImageViewMap[imagePicker.value]!!.opacity*100
        }

        opacitySlider.valueProperty().addListener{ _, oldVal, newVal ->
//            println("slider oldValue = $oldVal")
//            println("slider newValue = $newVal")
            fileToImageViewMap[imagePicker.value]!!.opacity = newVal.toInt() / 100.0
            if (isMakingGIF){
                if (oldVal.toInt()==100) addImageToArrayList()
                if (newVal.toInt() % comboFrameEvery.value ==0) addImageToArrayList()
//                if (oldVal.toInt()==100) makeImage("png")
//                if (newVal.toInt()%2==0) makeImage("png")
            }
        }

        bottomSlider.valueProperty().addListener { _, oldVal, newVal ->  //делаем видимой часть картинки в зависимости от положения нижнего бегунка
            val curX = (fileToImageViewMap[imagePicker.value]!!.image.width * newVal.toInt()) / 100
            fileToImageViewMap[imagePicker.value]!!.viewport = Rectangle2D(curX, 0.0, fileToImageViewMap[imagePicker.value]!!.image.width,
                                                                                                        fileToImageViewMap[imagePicker.value]!!.image.height)
            fileToImageViewMap[imagePicker.value]!!.translateX = curX * fileToImageViewMap[imagePicker.value]!!.scaleX //и сдвигаем картинку на нужное значение
            if (isMakingGIF) {
                if (oldVal.toInt() == 100) addImageToArrayList()
                if (newVal.toInt() % comboFrameEvery.value == 0) addImageToArrayList()
            }
        }
    }

    fun deleteCurImage(actionEvent: ActionEvent) {
        paneWithImages.children.remove(fileToImageViewMap[imagePicker.value])
        fileToImageViewMap[imagePicker.value]?.image = null
        fileToImageViewMap[imagePicker.value] =  null
        fileToImageViewMap.remove(imagePicker.value)
        imagePicker.items.remove(imagePicker.value)
        System.gc()
        val r = Runtime.getRuntime()
        r.freeMemory()
        if (imagePicker.items.isEmpty())  buttonBox.isDisable = true
    }

    fun curImageLayerUp(actionEvent: ActionEvent) {
        val i = paneWithImages.children.indexOf(fileToImageViewMap[imagePicker.value])
        paneWithImages.children[i].toFront()
    }

    fun curImageLayerDown(actionEvent: ActionEvent) {
        val i = paneWithImages.children.indexOf(fileToImageViewMap[imagePicker.value])
        paneWithImages.children[i].toBack()
    }

    private fun scaleImage(delta: Double, imageView: Node){
        if (imageView !is Rectangle){
            val xBeforeScale = imageView.boundsInParentProperty().get().minX
            val yBeforeScale = imageView.boundsInParentProperty().get().minY
            println("hbox minX in ParentProperty = ${imageView.boundsInParentProperty().get().minX}")
            println("hbox minX in LocalProperty = ${imageView.boundsInLocalProperty().get().minX}")
            imageView.scaleX += delta
            imageView.scaleY += delta
            val xAfterScale = imageView.boundsInParentProperty().get().minX
            val yAfterScale = imageView.boundsInParentProperty().get().minY
            val newWidth = imageView.boundsInParentProperty().get().width
//        if (imageView is ImageView) println("image width = ${(imageView as ImageView).image.width}")
//        if (imageView is HBox) println("hbox width after scale = $newWidth")
            println("xBeforeScale-xAfterScale = ${xBeforeScale - xAfterScale}")
            println("hbox minX in ParentProperty after scale = ${imageView.boundsInParentProperty().get().minX}")
            println("hbox minX in LocalProperty after scale = ${imageView.boundsInLocalProperty().get().minX}")
            imageView.translateX = imageView.translateX + xBeforeScale - xAfterScale
            imageView.translateY = imageView.translateY + yBeforeScale - yAfterScale
        }
    }
    fun onZoomPlus(actionEvent: ActionEvent) {
        for(imageView in paneWithImages.children){
            scaleImage(.1, imageView)
        }
    }

    fun onZoomMinus(actionEvent: ActionEvent) {
        for(imageView in paneWithImages.children){
            scaleImage(-.1, imageView)
        }
    }

    fun makeImage(type: String) {
        val snapshot: WritableImage = paneWithImages.snapshot(SnapshotParameters(), null)
        val file = File("$dirPath/img$imgCount.$type")
        imgCount++
        ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null),"$type", file)
//        val image = (stackPaneWithImages as ImageView).image
//        image.
    }

    fun addImageToArrayList(){//todo почитать про SnapshotParameters, чтобы сжать изображение
//        val sp = SnapshotParameters()
//        sp.transform = Transform.scale(5.0, 5.0)
        val snapshot: WritableImage = paneWithImages.snapshot(SnapshotParameters(), null)
        imagesArrayList.add(snapshot)
    }

    fun gifBtnClick(actionEvent: ActionEvent) {
        isMakingGIF = !isMakingGIF
        if (gifBtn.text =="start gif") beginGifWork()
        if (gifBtn.text =="stop gif") {
            val gifWork = GifWork(dirPath)
//            gifWork.makeGif()
            gifWork.makeGifFromArrayList(imagesArrayList, msBtwFrms.text.toInt())
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
//        if (isMakingGIF){
//           makeImage("png")
//        }
        Runtime.getRuntime().exec("ShareX/ShareX.exe")
    }

    private fun cropImage(originalImgage: BufferedImage, x: Int, y: Int, w: Int, h: Int): BufferedImage? {
            return originalImgage.getSubimage(x, y, w, h)
    }

    private fun cropImage2(image: BufferedImage, startX: Int, startY: Int, endX: Int, endY: Int): File {
        val img = image.getSubimage(startX, startY, endX, endY) //fill in the corners of the desired crop location here
        val copyOfImage = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_RGB)
        var currentPath: String = Paths.get(".").toAbsolutePath().normalize().toString()
//        println("curPath = ${currentPath}")
//        currentPath = currentPath.dropLastWhile { it!='\\' }
        println("curPath = ${currentPath}")
        val outputfile = File("$currentPath\\CroppedImages\\ImageCropped.png")
        ImageIO.write(img, "png", outputfile)
//        Image("file:${file.path}")
//        val g: Graphics = copyOfImage.createGraphics()
//        g.drawImage(img, 0, 0, null)
//        return copyOfImage //or use it however you want
//        return img
        return outputfile
    }
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        println("Program started")
        comboFrameEvery.items.addAll(1,2,3,4,5)
        comboFrameEvery.value = 5
    }

    fun onCropImage(actionEvent: ActionEvent) {
        val newImage = fileToImageViewMap[imagePicker.value]?.image
        var image: BufferedImage? = null
        val fileWithCropped = cropImage2(SwingFXUtils.fromFXImage(newImage, image), rect.x.toInt(), rect.y.toInt(), rect.width.toInt(), rect.height.toInt())
        deleteCurImage(ActionEvent())
        mainPane.center = null
        paneWithImages = AnchorPane()
        mainPane.center = paneWithImages
        val imageView = ImageView()
//        imageView.image = SwingFXUtils.toFXImage(image, null)
        imageView.image = Image("file:${fileWithCropped.path}")
        paneWithImages.children.add(imageView)
    }
}
