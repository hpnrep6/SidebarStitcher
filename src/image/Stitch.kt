package utils

import image.ImageDirectory
import java.awt.Color
import java.awt.image.BufferedImage

private fun alphaCompositeChannel(col: Int, base: Int, alpha: Float) : Int {
    var colF = col.toFloat() / 255f
    var baseF = base.toFloat() / 255f

    // Apply alpha composite algorithm, and clamp with 0 and 255
    return Math.max(0, Math.min(255, ((colF * alpha + (1f - alpha) * baseF) * 255f + 0.5f).toInt()))
}

private fun alphaComposite(a: Color, b: Color, alpha: Float) : Color {
    return Color(
        alphaCompositeChannel(a.red, b.red, alpha),
        alphaCompositeChannel(a.green, b.green, alpha),
        alphaCompositeChannel(a.blue, b.blue, alpha)
    )
}

fun stitch(batch: ImageDirectory) : BufferedImage {
    val height = batch.totalHeight
    val width = batch.width

    val images = batch.images

    val base = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    var offset = 0

    for (image in images) {
        var img = image.image

        appendImage(offset, img, base)

        offset += img.height
    }

    return base
}

private fun appendImage(offset: Int, toAppend: BufferedImage, base: BufferedImage) {
    var xApp = 0
    var yApp = 0
    var yBase = offset
    var yFinal = offset + toAppend.height

    while (yBase < yFinal) {
        xApp = 0
        for (xBase in 0..base.width - 1) {
            var rgb = toAppend.getRGB(xApp, yApp)
            base.setRGB(xBase, yBase, rgb)
            ++xApp
        }
        ++yApp
        ++yBase
    }
}

fun stitchWithSidebar(batch: ImageDirectory, sideBar: BufferedImage, fade: Boolean = true, fadeWidthInner: Int = 25, fadeWidthOuter: Int = 25) : BufferedImage {
    val height = batch.totalHeight
    val width = batch.width + sideBar.width - fadeWidthOuter

    val images = batch.images

    val base = BufferedImage(width , height, BufferedImage.TYPE_INT_ARGB)

    var offset = 0

    if (fade)
        for (image in images) {
            var img = image.image

            appendImageSidebarFade(offset, img, base, sideBar, fadeWidthInner, fadeWidthOuter)

            offset += img.height
        }
    else
        for (image in images) {
            var img = image.image

            appendImageSidebar(offset, img, base, sideBar)

            offset += img.height
        }

    return base
}

private fun appendImageSidebar(offset: Int, toAppend: BufferedImage, base: BufferedImage, sideBar: BufferedImage) {
    var sidebarOffset = offset % sideBar.height

    var xApp = 0
    var yApp = 0

    var xSide = 0
    var ySide = sidebarOffset

    var yBase = offset
    var yFinal = offset + toAppend.height

    while (yBase < yFinal) {
        xApp = 0
        for (xBase in 0..base.width - 1 - sideBar.width) {
            var rgb = toAppend.getRGB(xApp, yApp)
            base.setRGB(xBase, yBase, rgb)
            ++xApp
        }

        xSide = 0
        for (xBase in base.width - sideBar.width..base.width - 1) {
            var rgb = sideBar.getRGB(xSide, ySide)
            base.setRGB(xBase, yBase, rgb)
            ++xSide
        }

        ++yApp
        ++yBase

        ySide = ++ySide % sideBar.height
    }
}

private fun appendImageSidebarFade(offset: Int, toAppend: BufferedImage, base: BufferedImage, sideBar: BufferedImage, fadeWidthInner: Int, fadeWidthOuter: Int) {
    var sidebarOffset = offset % sideBar.height

    var xApp = 0
    var yApp = 0

    var xSide = 0
    var ySide = sidebarOffset

    var yBase = offset
    var yFinal = offset + toAppend.height

    var fadeIncrement: Float = 1f / (fadeWidthInner.toFloat() + fadeWidthOuter.toFloat())
    var alpha: Float = 0f
    var alphaReset = fadeIncrement * sideBar.width

    while (yBase < yFinal) {
        xApp = 0

        // Append main image
        for (xBase in 0..base.width - 1 - sideBar.width + fadeWidthOuter) {
            var rgb = toAppend.getRGB(xApp, yApp)
            base.setRGB(xBase, yBase, rgb)
            ++xApp
        }

        xSide = sideBar.width - 1
        alpha = alphaReset

        for (xBase in base.width - 1 downTo base.width - sideBar.width) {
            alpha -= fadeIncrement

            if (xBase < base.width - sideBar.width + fadeWidthOuter) {
                var rgb : Color = Color(sideBar.getRGB(xSide, ySide))

                var alphaApplied = alphaComposite(rgb, Color(base.getRGB(xBase, yBase)), alpha)

                base.setRGB(xBase, yBase, alphaApplied.rgb)
            } else if (alpha < 1) {
                // Only apply alpha composite algorithm if needed to improve performance
                var rgb : Color = Color(sideBar.getRGB(xSide, ySide))

                var alphaApplied = alphaComposite(rgb, Color(base.getRGB(xApp - 2, yBase)), alpha)

                base.setRGB(xBase, yBase, alphaApplied.rgb)
            } else {
                base.setRGB(xBase, yBase, sideBar.getRGB(xSide, ySide))
            }

            --xSide
        }

        ++yApp
        ++yBase

        ySide = ++ySide % sideBar.height
    }
}