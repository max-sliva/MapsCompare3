import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.StreamingGifWriter
import javafx.concurrent.Task
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.image.WritableImage
import javafx.scene.layout.BorderPane
import javafx.stage.Modality
import javafx.stage.Stage
import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration


class GifWork(private val pathToDir: String) {
    fun makeGifFromArrayList(imagesArrayList: ArrayList<WritableImage>, millis: Int) {
        val writer = StreamingGifWriter(Duration.ofMillis(millis.toLong()), false)
        val gif = writer.prepareStream("${pathToDir}/gif.gif", BufferedImage.TYPE_INT_ARGB)
        val dialog = Stage()
        dialog.title = "Saving gif"
       // dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL)
        val bar = ProgressBar(0.0)
        val pane = BorderPane()
        pane.center = bar
        pane.bottom = Label("")
        bar.progress = 0.0
        dialog.scene = Scene(pane, 250.0, 150.0)
        val task: Task<Int?> = object : Task<Int?>() {
            @Throws(Exception::class)
            override fun call(): Int {
                val iterations: Int = imagesArrayList.size
                var part = 100 / iterations
                println("progress = ${bar.progress}")
                for (image in imagesArrayList) {
                    val image = SwingFXUtils.fromFXImage(image, null)
                    val img = ImmutableImage.fromAwt(image)
                    gif.writeFrame(img)
                    updateProgress(part.toLong(), iterations.toLong())
                    part+=part
//                    Platform.runLater {
//                        bar.progress = bar.progress + part
//                    }
//                    println("progress = ${bar.progress}")
                }
                gif.close()
                imagesArrayList.clear()
//                writer.
                println("progress = All")
                return iterations
            }
        }
       // bar.progressProperty().bind(task.progressProperty())
        dialog.show()
        task.onSucceeded = EventHandler {
            println("Done!")
            pane.bottom = Label("Done making GIF. Please, close the window")
            dialog.close()
        }
        bar.progressProperty().bind(task.progressProperty())
        Thread(task).start()
//        gif.close()

    }

    fun makeGif(): String {
        val gifFile = String()
        val writer = StreamingGifWriter(Duration.ofMillis(100), false)
        val gif = writer.prepareStream("${pathToDir}/gif.gif", BufferedImage.TYPE_INT_ARGB)
//        gif.writeFrame(image0)
        val imageFiles = getAllImageFilesFromFolder(File(pathToDir))

        //Make sure that at least one image is present
        if (imageFiles!!.isEmpty()) {
            throw RuntimeException("No image files present!")
        }
        //Add all the images to gif
        for (imageFile in imageFiles) {
            val img = ImmutableImage.loader().fromFile(imageFile)
            gif.writeFrame(img)
        }
        gif.close()
        return gifFile
    }

    private fun getAllImageFilesFromFolder(directory: File): List<File> {
        //Get all the files from the folder
        val allFiles = directory.listFiles()
        if (allFiles == null || allFiles.size == 0) {
            throw RuntimeException("No files present in the directory: " + directory.absolutePath)
        }
        val acceptedImages: MutableList<File> = ArrayList()//Filter out only image files
        for (file in allFiles) {
            val fileExtension = file.name.substring(file.name.lastIndexOf(".") + 1)
            if (fileExtension.equals("jpg", ignoreCase = true) || fileExtension.equals("png", ignoreCase = true)) {
                acceptedImages.add(file)
            }
        }
        return acceptedImages //Return the filtered images
    }

//    @Throws(Exception::class)
//    private fun createAnimatedGifFromFolder(path: String) {
//        //The GIF image will be created with file name "gif_from_folder.gif"
//        FileOutputStream("my_animated_image.gif").use { outputStream ->
//            //Create GIF encoder with same dimension as of the source images
//            val encoder = GifEncoder(outputStream, 1920, 1200, 0)
//
//            //Get all the image files
//            val imageFiles = getAllImageFilesFromFolder(File(path))
//
//            //Make sure that at least one image is present
//            if (imageFiles!!.isEmpty()) {
//                throw RuntimeException("No image files present!")
//            }
//
//            //Add all the images to the GifEncoder
//            val options = ImageOptions()
//            for (imageFile in imageFiles) {
//                encoder.addImage(convertImageToArray(imageFile), options)
//            }
//
//            //Finish encoding and create the file
//            encoder.finishEncoding()
//        }
//    }
}