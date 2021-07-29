package image

import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import kotlin.collections.HashMap

fun initImageTypes() : HashMap<String, Int> {
    val map = hashMapOf<String, Int>()
    map[".jpg"] = 0
    map[".png"] = 0
    map[".gif"] = 0
    map[".bmp"] = 0
    map[".jpeg"] = 0
    map[".wbmp"] = 0

    return map
}

val imageTypes = initImageTypes()

class ImageDirectory(directory : File) {
    enum class readFileStatus {
        SUCCESS,
        NOT_DIR,
        FAILED,
        UNUNIFORM_WIDTH
    }

    private var dir = directory

    val images = mutableListOf<ImageFile>()

    var totalHeight : Int = 0
    var width : Int = 0

    fun setDir(directory: File) {
        dir = directory
        initialise()
    }

    /**
     * Read directory files
     */
    fun initialise() : readFileStatus {
        if (!dir.isDirectory) {
            return readFileStatus.NOT_DIR
        }

        images.clear()

        var empty = true

        try {
            for (file in dir.listFiles()) {
                if (extIsImage(file.name)) {
                    images.add(ImageFile(file))
                    empty = false
                }
            }
        } catch (e : Exception) {
            return readFileStatus.FAILED
        }

        if (empty)
            return readFileStatus.SUCCESS

        images.sortBy {
            imageFile -> imageFile.name
        }

        if (!checkWidth())
            return readFileStatus.UNUNIFORM_WIDTH

        getHeight()

        return readFileStatus.SUCCESS
    }

    fun getHeight() {
        totalHeight = 0

        for (image in images) {
            var img = image.image

            totalHeight += img.height
        }
    }

    fun checkWidth() : Boolean {
        var lastWidth = images[0].image.width

        for (i in 1..images.size - 1) {
            var img = images[i].image

            if (img.width != lastWidth)
                return false

            lastWidth = img.width
        }

        width = lastWidth

        return true
    }

    fun extIsImage(name: String) : Boolean {
        if (name.length >= ".png".length) {
            var last4 = name.substring(name.length - ".png".length).lowercase(Locale.ENGLISH)

            if (imageTypes.containsKey(last4)) {
                return true
            }

        }

        if (name.length >= ".jpeg".length) {
            var last5 = name.substring(name.length - ".jpeg".length).lowercase(Locale.ENGLISH)

            if (imageTypes.containsKey(last5)) {
                return true
            }
        }

        return false
    }
}