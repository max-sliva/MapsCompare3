import java.awt.BorderLayout
import java.awt.Graphics2D
import java.awt.Image
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
    println("11")
    val myFrame = JFrame().apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        title = "Tiff work"
        setSize(800, 600)
        setLocationRelativeTo(null)
        isVisible = true
    }
    val file = File("C:/IdeaProjects/at3_1m4_01.tiff")

    var imgIcon = ImageIcon()
    val image = ImageIO.read(file)
    println("file is read")
    println("img width = ${image.width}  height = ${image.height}")
    imgIcon.image = image.getScaledInstance(1400, -1, Image.SCALE_FAST)
    println("img width = ${image.width}  height = ${image.height}")

//    ImageIO.write(outputImage, formatName, new File(outputImagePath));
    println("img is scaled")
    val imgLabel = JLabel(imgIcon)
    println("label is made")
    myFrame.add(JScrollPane(imgLabel), BorderLayout.CENTER)
    println("label is added to frame")
    val southBox = Box(BoxLayout.X_AXIS)
    southBox.add(Box.createHorizontalGlue())
    val openBtn = JButton("Open tiff")
    southBox.add(openBtn)
    southBox.add(Box.createHorizontalGlue())
    val zoomPlusBtn = JButton("zoom +")
    zoomPlusBtn.addActionListener {
        imgIcon.image = image.getScaledInstance(imgIcon.iconWidth+100, -1, Image.SCALE_FAST)
//        image
        println("img is scaled")
        imgLabel.icon = imgIcon
        myFrame.validate()
//        imgLabel

    }
    southBox.add(zoomPlusBtn)
    southBox.add(Box.createHorizontalGlue())
    val zoomMinusBtn = JButton("zoom -")
    zoomMinusBtn.addActionListener {
        imgIcon.image = image.getScaledInstance(imgIcon.iconWidth-100, -1, Image.SCALE_FAST)
//        image
        println("img is scaled")
        imgLabel.icon = imgIcon
        myFrame.validate()
    }
    southBox.add(zoomMinusBtn)
    southBox.add(Box.createHorizontalGlue())
    myFrame.add(southBox, BorderLayout.SOUTH)
    myFrame.validate()
}