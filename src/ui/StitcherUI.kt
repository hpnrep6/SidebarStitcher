package ui

import image.ImageDirectory
import utils.stitch
import utils.stitchWithSidebar
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.lang.NumberFormatException
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

val commentStr = "}"

class StitcherUI {
    private lateinit var sourceButton: JButton
    private lateinit var saveButton: JButton
    private lateinit var sourceLabel: JLabel
    private lateinit var saveLabel: JLabel
    private lateinit var sideButton: JButton
    private lateinit var clearButton: JButton
    private lateinit var innerText: JTextField
    private lateinit var outerText: JTextField
    private lateinit var innerLabel: JLabel
    private lateinit var outerLabel: JLabel
    private lateinit var sideLabel: JLabel
    private lateinit var stitch: JButton
    private lateinit var stitchLabel: JLabel

    private lateinit var frame: JFrame

    init {
        frame = JFrame()

        sourceButton = JButton("Select directory of images to stitch")
        sourceLabel = JLabel("Source directory: none selected")

        saveButton = JButton("Select directory to save stitched image")
        saveLabel = JLabel("Save directory: none selected")

        sideButton = JButton("Select sidebar watermark image file")
        sideLabel = JLabel("Sidebar watermark file: none selected")

        innerLabel = JLabel("Enter inner sidebar fade width: 0")
        innerText = JTextField("")

        outerLabel = JLabel("Enter outer sidebar fade width: 0")
        outerText = JTextField("")

        clearButton = JButton("Clear saved settings")

        stitch = JButton("Stitch!")
        stitchLabel = JLabel("Stitch status: None")

        frame.add(sourceButton)
        frame.add(sourceLabel)

        frame.add(saveButton)
        frame.add(saveLabel)

        frame.add(sideButton)
        frame.add(sideLabel)

        frame.add(innerLabel)
        frame.add(innerText)

        frame.add(outerLabel)
        frame.add(outerText)

        frame.add(stitch)
        frame.add(stitchLabel)

        frame.add(clearButton)

        frame.layout = GridLayout(13, 1)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.isVisible = true
        frame.setSize(600, 1000)

        sourceButton.addActionListener(::sourceListener)

        saveButton.addActionListener(::saveListener)

        sideButton.addActionListener(::sideListener)

        innerText.addActionListener(::innerListener)

        outerText.addActionListener(::outerListener)

        stitch.addActionListener(::stitch)

        clearButton.addActionListener(::reset)

        readSettingsFile()
    }

    var source: File? = null
    var save: File? = null
    var side: File? = null

    var inner: Int = 0
    var outer: Int = 0

    fun sourceListener(event: ActionEvent) {
        var selected = selectDirectory()

        if (selected == null)
            return

        setSourceDir(selected)
    }

    fun setSourceDir(file: File) {
        var name = file.toString()

        sourceLabel.text = "Source directory: $name"
        source = file
    }

    fun saveListener(event: ActionEvent) {
        var selected = selectDirectory()

        if (selected == null)
            return

        setSaveDir(selected)
    }

    fun setSaveDir(file: File) {
        var name = file.toString()

        saveLabel.text = "Save directory: $name"
        save = file
    }

    fun sideListener(event: ActionEvent) {
        var selected = selectImageFile()

        if (selected == null)
            return

        setSideFile(selected)
    }

    fun setSideFile(file: File) {
        var name = file.toString()

        sideLabel.text = "Sidebar file: $name"
        side = file
    }

    fun innerListener(event: ActionEvent) {
        var txt = innerText.text

        var int: Int = 0

        try {
            int = txt.toInt()
        } catch (e: NumberFormatException) {
            innerLabel.text = "Enter inner sidebar fade width: invalid integer"
            return
        }

        int = 0.coerceAtLeast(int)

        innerLabel.text = "Enter inner sidebar fade width: $int"

        inner = int
    }

    fun outerListener(event: ActionEvent) {
        var txt = outerText.text

        var int: Int = 0

        try {
            int = txt.toInt()
        } catch (e: NumberFormatException) {
            outerLabel.text = "Enter outer sidebar fade width: invalid integer"
            return
        }

        int = 0.coerceAtLeast(int)

        outerLabel.text = "Enter outer sidebar fade width: $int"

        outer = int
    }

    fun stitch(event: ActionEvent) {
        if (source == null) {
            stitchLabel.text = "No source directory selected"
            return
        }

        if (save == null) {
            stitchLabel.text = "No save directory selected"
            return
        }

        stitchLabel.text = "Stitching. Please wait..."

        var imageDir = ImageDirectory(source!!)

        var attempt = imageDir.initialise()

        when (attempt) {
            ImageDirectory.readFileStatus.SUCCESS -> {
                var time = Date().time
                var name = "stitched_"

                if (side == null) {
                    var stitched = stitch(imageDir)

                    try {
                        saveImage(stitched, save!!, "$name$time")
                    } catch (e: IOException) {
                        stitchLabel.text = "Error: Failed saving stitched image"
                        return
                    }
                } else {
                    var sidebar: BufferedImage? = null

                    try {
                        sidebar = readImage(side!!)
                    } catch (e: IOException) {
                        stitchLabel.text = "Error: Failed reading sidebar watermark image"
                        return
                    }

                    var fade = ! (inner == 0 && outer == 0)

                    var stitched = stitchWithSidebar(imageDir, sidebar, fade, inner, outer)

                    try {
                        name = "stitched-sb_"
                        saveImage(stitched, save!!, "$name$time")
                    } catch (e: IOException) {
                        stitchLabel.text = "Error: Failed saving stitched image"
                        return
                    }
                }

                writeSettingsFile()
                stitchLabel.text = "Success: Stitched image saved at ${save.toString()}${File.separator}$name$time"
            }

            ImageDirectory.readFileStatus.NOT_DIR -> {
                stitchLabel.text = "Error: Please select an actual source directory"
            }

            ImageDirectory.readFileStatus.FAILED -> {
                stitchLabel.text = "Error: Failed trying to read images"
            }

            ImageDirectory.readFileStatus.UNUNIFORM_WIDTH -> {
                stitchLabel.text = "Error: Images must have the same width"
            }
        }
    }

    fun reset(event: ActionEvent) {
        sourceLabel.text = "Source directory: none selected"

        saveLabel.text = "Save directory: none selected"

        sideLabel.text = "Sidebar watermark file: none selected"

        innerLabel.text = "Enter inner sidebar fade width: 0"

        outerLabel.text = "Enter outer sidebar fade width: 0"

        source = null
        save = null
        side = null

        inner = 0
        outer = 0

        try {
            var settings = File("WatermarkStitcher.config")
            settings.delete()
        } catch (e: Exception) {

        }
    }

    fun selectDirectory() : File? {
        var chooser = JFileChooser()
        chooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        chooser.isAcceptAllFileFilterUsed = false

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.selectedFile
        } else {
            return null
        }
    }

    fun selectImageFile() : File? {
        var chooser = JFileChooser()

        chooser.fileFilter = FileNameExtensionFilter("Image File",
            "png", "jpeg", "jpg", "bmp", "gif", "wbmp")

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.selectedFile
        } else {
            return null
        }
    }

    @Throws(IOException::class)
    fun saveImage(image: BufferedImage, directory: File, name: String) {
        var file: File = File("${directory.toString()}${File.separator}$name.png")

        ImageIO.write(image, "png", file)
    }

    @Throws(IOException::class)
    fun readImage(file: File): BufferedImage {
        return ImageIO.read(file)
    }

    fun writeSettingsFile() {
        var settings: File = File("WatermarkStitcher.config")

        var writer = PrintWriter(settings)

        writeNullableSetting(source, "Source directory", writer)
        writeNullableSetting(save, "Save directory", writer)
        writeNullableSetting(side, "Sidebar image file", writer)
        writer.println("${inner}${commentStr}Inner width")
        writer.println("${outer}${commentStr}Outer width")

        writer.flush()
        writer.close()
    }

    fun writeNullableSetting(obj: Any?, desc: String, writer: PrintWriter) {
        if (obj == null) {
            writer.println("$commentStr$desc")
        } else {
            writer.println("$obj$commentStr$desc")
        }
    }

    fun readSettingsFile() {
        var settings: File = File("WatermarkStitcher.config")

        if (!settings.exists())
            return

        try {
            var lines = settings.readLines()

            if (lines.size != 5)
                return

            for (line in lines) {
                if (!line.contains(commentStr))
                    return
            }

            var srcStr = lines[0].substring(0, lines[0].indexOf(commentStr))
            var sveStr = lines[1].substring(0, lines[1].indexOf(commentStr))


            var sdeStr = lines[2].substring(0, lines[2].indexOf(commentStr))

            var innStr = lines[3].substring(0, lines[3].indexOf(commentStr))
            var outStr = lines[4].substring(0, lines[4].indexOf(commentStr))

            if (
                srcStr.isEmpty() ||
                sveStr.isEmpty() ||
                innStr.isEmpty() ||
                outStr.isEmpty()
            ) {
                return
            }

            var src = File(srcStr)

            if (src.isDirectory && src.exists()) {
                setSourceDir(src)
            }

            var sve = File(sveStr)

            if (sve.isDirectory && sve.exists()) {
                setSaveDir(sve)
            }

            var sde = File(sdeStr)

            if (sde.isFile && sde.exists()) {
                setSideFile(sde)
            }

            var inn = innStr.toInt()
            var out = outStr.toInt()

            inner = inn
            outer = out

            innerLabel.text = "Enter inner sidebar fade width: $inner"
            outerLabel.text = "Enter outer sidebar fade width: $outer"

        } catch (e: Exception) {
            return
        }

    }

}