package image

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ImageFile(file: File) {
    lateinit var name : String
    lateinit var image: BufferedImage

    var sortRanking: Long = 0

    init {
        image = ImageIO.read(file)
        name = file.name

    }

    override fun toString(): String {
        return "ImageFile: $name"
    }

}