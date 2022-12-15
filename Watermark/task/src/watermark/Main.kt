package watermark

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.lang.NumberFormatException
import java.util.*
import javax.imageio.IIOException
import javax.imageio.ImageIO

class ColorImageException : Exception()
class BitImageException : Exception()
class WIDimException : Exception()
class OutOfRangeException : Exception()
class PositionOutOfRangeException : Exception()
class ExtensionException : Exception()
class ColorWIException : Exception()
class BitWIException : Exception()

/**
 * Returns the string representation of a transparency value
 */
fun transparencyCheck(transparency: Int): String {
    val transparencyType = when (transparency){
        1 -> "OPAQUE"
        2 -> "BITMASK"
        3 -> "TRANSLUCENT"
        else -> "Wrong value"
    }
    return transparencyType
}

/**
 * Merges an image with a transparent watermark
 */
fun imageAlphaWatermark(
    inputImage: BufferedImage,
    watermark: BufferedImage,
    outputImage: BufferedImage,
    grid: Boolean,
    position: List<Int>,
    transparency: Int): BufferedImage {
    for (x in 0 until outputImage.width) {
        for (y in 0 until outputImage.height) {
            val i = Color(inputImage.getRGB(x, y))
            if (!grid) {
                if (x in position[0] until position[0] + watermark.width &&
                    y in position[1] until position[1] + watermark.height
                ) {
                    val w = Color(watermark.getRGB(x - position[0], y - position[1]), true)
                    if (w.alpha == 0) {
                        outputImage.setRGB(x, y, i.rgb)
                    } else if (w.alpha == 255) {
                        val newColor = Color(
                            (transparency * w.red + (100 - transparency) * i.red) / 100,
                            (transparency * w.green + (100 - transparency) * i.green) / 100,
                            (transparency * w.blue + (100 - transparency) * i.blue) / 100
                        )
                        outputImage.setRGB(x, y, newColor.rgb)
                    }
                } else {
                    outputImage.setRGB(x, y, i.rgb)
                }
            } else {
                val w = Color(watermark.getRGB(x % watermark.width, y % watermark.height), true)
                if (w.alpha == 0) {
                    outputImage.setRGB(x, y, i.rgb)
                } else if (w.alpha == 255) {
                    val newColor = Color(
                        (transparency * w.red + (100 - transparency) * i.red) / 100,
                        (transparency * w.green + (100 - transparency) * i.green) / 100,
                        (transparency * w.blue + (100 - transparency) * i.blue) / 100
                    )
                    outputImage.setRGB(x, y, newColor.rgb)
                }
            }
        }
    }
    return outputImage
}

/**
 * Merges two images
 */
fun imageMergeWatermark(
    inputImage: BufferedImage,
    watermark: BufferedImage,
    outputImage: BufferedImage,
    grid: Boolean,
    position: List<Int>,
    transparency: Int): BufferedImage {
    for (x in 0 until outputImage.width) {
        for (y in 0 until outputImage.height) {
            val i = Color(inputImage.getRGB(x, y))
            if (!grid) {
                if (x in position[0] until position[0] + watermark.width &&
                    y in position[1] until position[1] + watermark.height
                ) {
                    val w = Color(watermark.getRGB(x - position[0], y - position[1]))
                    val newColor = Color(
                        (transparency * w.red + (100 - transparency) * i.red) / 100,
                        (transparency * w.green + (100 - transparency) * i.green) / 100,
                        (transparency * w.blue + (100 - transparency) * i.blue) / 100
                    )
                    outputImage.setRGB(x, y, newColor.rgb)
                } else {
                    outputImage.setRGB(x, y, i.rgb)
                }
            } else {
                val w = Color(watermark.getRGB(x % watermark.width, y % watermark.height))
                val newColor = Color(
                    (transparency * w.red + (100 - transparency) * i.red) / 100,
                    (transparency * w.green + (100 - transparency) * i.green) / 100,
                    (transparency * w.blue + (100 - transparency) * i.blue) / 100
                )
                outputImage.setRGB(x, y, newColor.rgb)
            }
        }
    }
    return outputImage
}

/**
 * Merges two images, for the second one the predefined color becomes transparent
 */
fun imageTransparentColorWatermark(
    inputImage: BufferedImage,
    watermark: BufferedImage,
    outputImage: BufferedImage,
    grid: Boolean,
    position: List<Int>,
    transparency: Int,
    colorsList: List<Int>): BufferedImage {
    for (x in 0 until outputImage.width) {
        for (y in 0 until outputImage.height) {
            val i = Color(inputImage.getRGB(x, y))
            if (!grid) {
                if (x in position[0] until position[0] + watermark.width &&
                    y in position[1] until position[1] + watermark.height
                ) {
                    val w = Color(watermark.getRGB(x - position[0], y - position[1]))
                    if (w.red == colorsList[0] && w.green == colorsList[1] && w.blue == colorsList[2]) {
                        outputImage.setRGB(x, y, i.rgb)
                    } else {
                        val newColor = Color(
                            (transparency * w.red + (100 - transparency) * i.red) / 100,
                            (transparency * w.green + (100 - transparency) * i.green) / 100,
                            (transparency * w.blue + (100 - transparency) * i.blue) / 100
                        )
                        outputImage.setRGB(x, y, newColor.rgb)
                    }
                } else {
                    outputImage.setRGB(x, y, i.rgb)
                }
            } else {
                val w = Color(watermark.getRGB(x % watermark.width, y % watermark.height))
                if (w.red == colorsList[0] && w.green == colorsList[1] && w.blue == colorsList[2]) {
                    outputImage.setRGB(x, y, i.rgb)
                } else {
                    val newColor = Color(
                        (transparency * w.red + (100 - transparency) * i.red) / 100,
                        (transparency * w.green + (100 - transparency) * i.green) / 100,
                        (transparency * w.blue + (100 - transparency) * i.blue) / 100
                    )
                    outputImage.setRGB(x, y, newColor.rgb)
                }
            }
        }
    }
    return outputImage
}

fun mergeImages() {
    var fileInput = ""
    try {
        // creating input image
        println("Input the image filename:")
        fileInput = readln()
        val inputImage = ImageIO.read(File(fileInput))
        val colorModelImage = inputImage.colorModel.numColorComponents
        val bitsImage = inputImage.colorModel.pixelSize
        if (colorModelImage != 3) throw ColorImageException()
        if (bitsImage != 24) { if (bitsImage != 32) throw BitImageException() }

        // creating watermark image
        println("Input the watermark image filename:")
        fileInput = readln()
        val watermark = ImageIO.read(File(fileInput))
        val colorModelWatermark = watermark.colorModel.numColorComponents
        val bitsWatermark = watermark.colorModel.pixelSize
        if (colorModelWatermark != 3) throw ColorWIException()
        if (bitsWatermark != 24) { if (bitsWatermark != 32) throw BitWIException() }

        // checking the size of the watermark, it must be smaller than the image size
        if (inputImage.width < watermark.width ||
            inputImage.height < watermark.height
        ) throw WIDimException()

        // checking for watermark transparency
        var alpha = false
        var colorsList = listOf<Int>()
        if (transparencyCheck(watermark.transparency) == "TRANSLUCENT") {
            println("Do you want to use the watermark's Alpha channel?")
            if (readln().lowercase(Locale.getDefault()) == "yes") alpha = true

        // asking for using a color which could be transparent
        } else {
            println("Do you want to set a transparency color?")
            if (readln().lowercase(Locale.getDefault()) == "yes") {
                println("Input a transparency color ([Red] [Green] [Blue]):")
                try {
                    colorsList = readln().split(" ").map { it.toInt() }
                    if (colorsList.size != 3) throw Exception()
                    for (i in colorsList) {
                        if (i !in 0..255) throw Exception()
                    }
                } catch (e: Exception) {
                    println("The transparency color input is invalid.")
                    return
                }
            }
        }

        // setting watermark transparency
        val transparency: Int
        try {
            println("Input the watermark transparency percentage (Integer 0-100):")
            transparency = readln().toInt()
            if (transparency !in 0..100) throw OutOfRangeException()
        } catch (e: OutOfRangeException) {
            println("The transparency percentage is out of range.")
            return
        } catch (e: NumberFormatException) {
            println("The transparency percentage isn't an integer number.")
            return
        }

        // setting watermark position
        println("Choose the position method (single, grid):")
        val positionInput = readln().lowercase(Locale.getDefault())
        var positionCoordinates = listOf<Int>()
        var grid = false
        when (positionInput) {
            "single" -> {
                val diffX = inputImage.width - watermark.width
                val diffY = inputImage.height - watermark.height
                println("Input the watermark position ([x 0-$diffX] [y 0-$diffY]):")
                try {
                    positionCoordinates = readln().split(" ").map { it.toInt() }
                    if (positionCoordinates.size != 2) throw Exception()
                    if (positionCoordinates[0] !in 0..diffX || positionCoordinates[1] !in 0..diffY) {
                        throw PositionOutOfRangeException()
                    }
                } catch (e: PositionOutOfRangeException) {
                    println("The position input is out of range.")
                    return
                }catch (e: Exception) {
                    println("The position input is invalid.")
                    return
                }
            }
            "grid" -> grid = true
            else -> {
                println("The position method input is invalid.")
                return
            }
        }

        // creating output image
        println("Input the output image filename (jpg or png extension):")
        val outputImageName = readln()
        val extension = outputImageName.substring(outputImageName.length - 3)
            if (extension != "jpg") { if (extension != "png") throw ExtensionException() }
        var outputImage = BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)

        // merging images in 3 different ways
        outputImage = if (alpha) {
            imageAlphaWatermark(inputImage, watermark, outputImage, grid, positionCoordinates, transparency)
        } else if (colorsList.isNotEmpty()) {
            imageTransparentColorWatermark(inputImage, watermark, outputImage, grid, positionCoordinates, transparency, colorsList)
        } else {
            imageMergeWatermark(inputImage, watermark, outputImage, grid, positionCoordinates, transparency)
        }

        // saving new image
        ImageIO.write(outputImage, extension, File(outputImageName))
        println("The watermarked image $outputImageName has been created.")

    } catch (e: ColorImageException) {
        println("The number of image color components isn't 3.")
    } catch (e: BitImageException) {
        println("The image isn't 24 or 32-bit.")
    } catch (e: ColorWIException) {
        println("The number of watermark color components isn't 3.")
    } catch (e: BitWIException) {
        println("The watermark isn't 24 or 32-bit.")
    } catch (e: WIDimException) {
        println("The watermark's dimensions are larger.")
    } catch (e: OutOfRangeException) {
        println("The transparency percentage is out of range.")
    } catch (e: ExtensionException) {
        println("The output file extension isn't \"jpg\" or \"png\".")
    } catch (e: IIOException) {
        println("The file $fileInput doesn't exist.")
    }
}

fun main() {
    mergeImages()
}