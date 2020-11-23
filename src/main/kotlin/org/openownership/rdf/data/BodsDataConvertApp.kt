package org.openownership.rdf.data

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import java.io.File

/**
 * @author Cosmin Marginean
 */
object BodsDataConvertApp {

    @JvmStatic
    fun main(args: Array<String>) {
        BodsDataConvertRunner().main(args)
    }
}

class BodsDataConvertRunner : CliktCommand() {
    private val input by option("--input").required()
    private val output by option("--output")

    override fun run() {
        val out = if (output != null) output else input.substring(0, input.lastIndexOf(".")) + ".ttls"
        BodsDataLoader.jsonlToRdf(File(input), File(out))
    }
}
