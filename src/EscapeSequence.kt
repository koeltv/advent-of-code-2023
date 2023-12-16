@file:Suppress("unused")


/**
 * The [EscapeSequence] class represents an escape sequence that can be used to modify the output of text in a console.
 *
 * @property code The escape sequence code.
 */
open class EscapeSequence private constructor(private val code: String) {
    override fun toString(): String = code

    companion object {
        /**
         * Color end string, color reset
         */
        val Reset: EscapeSequence = EscapeSequence("\u001b[0m")

        /**
         * Clear current line
         */
        val ClearLine: EscapeSequence = EscapeSequence("\r\u001b[K")

        /**
         * Clear the whole terminal
         */
        val ClearTerminal: EscapeSequence = EscapeSequence("\u001b[H\u001b[2J")
    }

    class Color private constructor(code: String) : EscapeSequence(code) {
        companion object {
            // Regular Colors (no bold, background color, etc.)
            val Black: Color = Color("\u001b[0;30m")
            val Red: Color = Color("\u001b[0;31m")
            val Green: Color = Color("\u001b[0;32m")
            val Yellow: Color = Color("\u001b[0;33m")
            val Blue: Color = Color("\u001b[0;34m")
            val Magenta: Color = Color("\u001b[0;35m")
            val Cyan: Color = Color("\u001b[0;36m")
            val White: Color = Color("\u001b[0;37m")

            // Bold
            val BlackBold: Color = Color("\u001b[1;30m")
            val RedBold: Color = Color("\u001b[1;31m")
            val GreenBold: Color = Color("\u001b[1;32m")
            val YellowBold: Color = Color("\u001b[1;33m")
            val BlueBold: Color = Color("\u001b[1;34m")
            val MagentaBold: Color = Color("\u001b[1;35m")
            val CyanBold: Color = Color("\u001b[1;36m")
            val WhiteBold: Color = Color("\u001b[1;37m")

            // Underline
            val BlackUnderlined: Color = Color("\u001b[4;30m")
            val RedUnderlined: Color = Color("\u001b[4;31m")
            val GreenUnderlined: Color = Color("\u001b[4;32m")
            val YellowUnderlined: Color = Color("\u001b[4;33m")
            val BlueUnderlined: Color = Color("\u001b[4;34m")
            val MagentaUnderlined: Color = Color("\u001b[4;35m")
            val CyanUnderlined: Color = Color("\u001b[4;36m")
            val WhiteUnderlined: Color = Color("\u001b[4;37m")

            // High Intensity
            val BlackBright: Color = Color("\u001b[0;90m")
            val RedBright: Color = Color("\u001b[0;91m")
            val GreenBright: Color = Color("\u001b[0;92m")
            val YellowBright: Color = Color("\u001b[0;93m")
            val BlueBright: Color = Color("\u001b[0;94m")
            val MagentaBright: Color = Color("\u001b[0;95m")
            val CyanBright: Color = Color("\u001b[0;96m")
            val WhiteBright: Color = Color("\u001b[0;97m")

            // Bold High Intensity
            val BlackBoldBright: Color = Color("\u001b[1;90m")
            val RedBoldBright: Color = Color("\u001b[1;91m")
            val GreenBoldBright: Color = Color("\u001b[1;92m")
            val YellowBoldBright: Color = Color("\u001b[1;93m")
            val BlueBoldBright: Color = Color("\u001b[1;94m")
            val MagentaBoldBright: Color = Color("\u001b[1;95m")
            val CyanBoldBright: Color = Color("\u001b[1;96m")
            val WhiteBoldBright: Color = Color("\u001b[1;97m")
        }
    }

    class BackgroundColor private constructor(code: String) : EscapeSequence(code) {
        companion object {
            // Background
            val BlackBackground: BackgroundColor = BackgroundColor("\u001b[40m")
            val RedBackground: BackgroundColor = BackgroundColor("\u001b[41m")
            val GreenBackground: BackgroundColor = BackgroundColor("\u001b[42m")
            val YellowBackground: BackgroundColor = BackgroundColor("\u001b[43m")
            val BlueBackground: BackgroundColor = BackgroundColor("\u001b[44m")
            val MagentaBackground: BackgroundColor = BackgroundColor("\u001b[45m")
            val CyanBackground: BackgroundColor = BackgroundColor("\u001b[46m")
            val WhiteBackground: BackgroundColor = BackgroundColor("\u001b[47m")

            // High Intensity backgrounds
            val BlackBackgroundBright: BackgroundColor = BackgroundColor("\u001b[0;100m")
            val RedBackgroundBright: BackgroundColor = BackgroundColor("\u001b[0;101m")
            val GreenBackgroundBright: BackgroundColor = BackgroundColor("\u001b[0;102m")
            val YellowBackgroundBright: BackgroundColor = BackgroundColor("\u001b[0;103m")
            val BlueBackgroundBright: BackgroundColor = BackgroundColor("\u001b[0;104m")
            val MagentaBackgroundBright: BackgroundColor = BackgroundColor("\u001b[0;105m")
            val CyanBackgroundBright: BackgroundColor = BackgroundColor("\u001b[0;106m")
            val WhiteBackgroundBright: BackgroundColor = BackgroundColor("\u001b[0;107m")
        }
    }
}

fun colorPrint(message: Any?, color: EscapeSequence.Color = EscapeSequence.Color.Black) {
    print("${color}${message}${EscapeSequence.Reset}")
}

fun String.withColor(color: EscapeSequence.Color = EscapeSequence.Color.Black): String {
    return "${color}${this}${EscapeSequence.Reset}"
}

fun Char.withColor(color: EscapeSequence.Color = EscapeSequence.Color.Black): String {
    return "${color}${this}${EscapeSequence.Reset}"
}

fun colorPrintln(message: Any?, color: EscapeSequence.Color = EscapeSequence.Color.Black) {
    println("${color}${message}${EscapeSequence.Reset}")
}

fun boldPrint(message: Any?) = colorPrint(message, EscapeSequence.Color.WhiteBoldBright)

fun clearLine() = print(EscapeSequence.ClearLine)

fun clearTerminal() = print(EscapeSequence.ClearTerminal)

fun progressBar(x: Int, outOf: Int, color: EscapeSequence.Color = EscapeSequence.Color.Green) {
    clearLine()
    val progress = ((x.toDouble() / outOf.toDouble()) * 100).toInt()
    colorPrint("[" + "#".repeat(progress) + " ".repeat(100 - progress) + "]", color)
    if (progress >= 100) println()
}