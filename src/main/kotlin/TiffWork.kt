import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.*
import javax.swing.*


class TiffWork(private val file: File) {
    fun readFileToBufferedImage(): BufferedImage{
        println("Start tiff work")
        val image = ImageIO.read(file)
        println("file is read")
        println("img width = ${image.width}  height = ${image.height}")
        return image
    }
//todo add function to convert to png
//todo add function to crop image
}

@Throws(IOException::class)
fun resize(
    inputImagePath: String?,
    outputImagePath: String, scaledWidth: Int, scaledHeight: Int
) {
    // reads input image
    val inputFile = File(inputImagePath)
    val inputImage = ImageIO.read(inputFile)

    // creates output image
    val outputImage = BufferedImage(
        scaledWidth,
        scaledHeight, inputImage.type
    )

    // scales the input image to the output image
    val g2d: Graphics2D = outputImage.createGraphics()
    g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null)
    g2d.dispose()

    // extracts extension of output file
    val formatName = outputImagePath.substring(
        outputImagePath
            .lastIndexOf(".") + 1
    )

    // writes to output file
    ImageIO.write(outputImage, formatName, File(outputImagePath))
}

fun main(){
    println("Start TiffWork")
    val myFrame = JFrame().apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        title = "Tiff work"
        setSize(800, 600)
        setLocationRelativeTo(null)
        isVisible = true
    }
//    val file = File("C:/IdeaProjects/at3_1m4_01.tiff")
//
//    var imgIcon = ImageIcon()
//    val image = ImageIO.read(file)
//    println("file is read")
//    println("img width = ${image.width}  height = ${image.height}")
//    imgIcon.image = image.getScaledInstance(1400, -1, Image.SCALE_FAST)
//    println("img width = ${image.width}  height = ${image.height}")
//
////    ImageIO.write(outputImage, formatName, new File(outputImagePath));
//    println("img is scaled")
//    val imgLabel = JLabel(imgIcon)
//    println("label is made")
//    myFrame.add(JScrollPane(imgLabel), BorderLayout.CENTER)
//    println("label is added to frame")
    val southBox = Box(BoxLayout.X_AXIS)
    southBox.add(Box.createHorizontalGlue())
    var file: File
    var imgIcon = ImageIcon()
    var image: BufferedImage? = null
    var imgLabel: JLabel? = null
    var dragStartX = 0
    var dragStartY = 0
    var oldX = 0
    var oldY = 0
    var inDrawingRect = false
    var grForLabel2d: Graphics2D? = null
    var square = Rectangle(1,1,0,0)
    var a = object: MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            println("something")
        }

        override fun mousePressed(e: MouseEvent?) {
            super.mousePressed(e)
            inDrawingRect = true
            dragStartX = e!!.x
            dragStartY = e!!.y
            println("inDrawingRect = $inDrawingRect")
            grForLabel2d = imgLabel?.graphics as Graphics2D
            val pen = BasicStroke(2F,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND)
            grForLabel2d?.stroke = pen
            grForLabel2d?.color = Color.RED
            square.setLocation(dragStartX, dragStartY)
        }

        override fun mouseDragged(e: MouseEvent?) {
//            println("mouse x = ${e?.x}  mouse y = ${e?.y} ")
            imgLabel?.icon = null
            imgLabel?.icon = imgIcon
//            grForLabel2d.drawRect(dragStartX, dragStartY, e!!.x-dragStartX, e!!.y-dragStartY)
            square.size = Dimension(e!!.x-dragStartX, e!!.y-dragStartY)
            grForLabel2d?.draw(square)
            super.mouseDragged(e)
        }

        override fun mouseReleased(e: MouseEvent?) {
            super.mouseReleased(e)
            println("inDrawingRect = $inDrawingRect")
            square.size = Dimension(e!!.x-dragStartX, e!!.y-dragStartY)
            grForLabel2d?.draw(square)
            inDrawingRect = false
        }
    }
//    imgLabel?.addMouseListener(a)
//    imgIcon?.addMouseListener(a)
    val cropBtn = JButton("cropImage")
    val openBtn = JButton("Open tiff")
    openBtn.addActionListener {
//        file = File("C:/IdeaProjects/at3_1m4_01.tiff")
        file = readTiffFromFile(myFrame)
        println("file path = ${file.path}")
        if (!file.path.equals("nullnull")){
            cropBtn.isEnabled = true
            image = ImageIO.read(file)
            println("file is read")
            println("img width = ${image?.width}  height = ${image?.height}")
            imgIcon.image = image?.getScaledInstance(1400, -1, Image.SCALE_FAST)
            println("img width = ${image?.width}  height = ${image?.height}")
//    ImageIO.write(outputImage, formatName, new File(outputImagePath));
            println("img is scaled")
            imgLabel = JLabel(imgIcon)
            imgLabel!!.addMouseListener(a)
            imgLabel?.addMouseMotionListener(a)
            println("label is made")
            myFrame.add(JScrollPane(imgLabel), BorderLayout.CENTER)
            println("label is added to frame")
            myFrame.validate()
        }
    }
    southBox.add(openBtn)
    southBox.add(Box.createHorizontalGlue())
    val zoomPlusBtn = JButton("zoom +")
    zoomPlusBtn.addActionListener {
        imgIcon.image = image?.getScaledInstance(imgIcon.iconWidth+100, -1, Image.SCALE_FAST)
        println("img is scaled")
        imgLabel?.icon = null
        imgLabel?.icon = imgIcon
        myFrame.validate()
//        myFrame.repaint()
    }
    southBox.add(zoomPlusBtn)
    southBox.add(Box.createHorizontalGlue())
    val zoomMinusBtn = JButton("zoom -")
    zoomMinusBtn.addActionListener {
        imgIcon.image = image?.getScaledInstance(imgIcon.iconWidth-100, -1, Image.SCALE_FAST)
        println("img is scaled")
        imgLabel?.icon = null
        imgLabel?.icon = imgIcon
        myFrame.validate()
//        myFrame.repaint()
    }
    southBox.add(zoomMinusBtn)
    southBox.add(Box.createHorizontalGlue())
    cropBtn.addActionListener {
//        todo make crop for image

    }
    cropBtn.isEnabled = false
    southBox.add(cropBtn)
    southBox.add(Box.createHorizontalGlue())
    myFrame.add(southBox, BorderLayout.SOUTH)
    myFrame.validate()
}

fun readTiffFromFile(myFrame: JFrame): File {
    var fileDialog = FileDialog(myFrame)
    fileDialog.mode = FileDialog.LOAD //для диалога устанавливаем режим Загрузки
    fileDialog.title = "Открыть файл" //делаем ему заголовок
    fileDialog.file = "*.tiff"
    fileDialog.isVisible = true //делаем его видимым
//    if (fileDialog.)
    val fileName = fileDialog.directory+fileDialog.file //получаем из него файл с полным путем
    println(fileName)
    return File(fileName)
}
