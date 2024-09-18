import kotlin.math.*

/**
fun printGaussian(width: Double, height: Double) {
    val stdDev = width / 4.0
    val rangeX = -5.0 * stdDev..5.0 * stdDev
    val rangeY = -0.5 * height..1.5 * height


    val values = MutableList(rangeY.count()) { MutableList(rangeX.count()) { 0.0 } }

    for (i in values.indices) {
        val y = rangeY.elementAt(i)
        for (j in values[i].indices) {
            val x = rangeX.elementAt(j)
            val exponent = -0.5 * (x * x / (0.25 * width * width) + y * y / (0.25 * height * height))
            values[i][j] = exp(exponent)
        }
    }

    for (row in values) {
        for (value in row) {
            val scaledValue = (value * 10).toInt()
            print(" ".repeat(10 - scaledValue) + "*".repeat(scaledValue) + "  ")
        }
        println()
    }
}
*/

fun main() {
    //printGaussian(10.0, 5.0) // Beispielwerte für width und height
}
