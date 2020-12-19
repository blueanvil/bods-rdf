package org.openownership.rdf.data

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.eclipse.rdf4j.rio.RDFFormat
import java.io.File

/**
 * @author Cosmin Marginean
 */
object BodsJsonToRdfApp {

    @JvmStatic
    fun main(args: Array<String>) {
        BodsJsonToRdfCmd().main(args)
    }
}

class BodsJsonToRdfCmd : CliktCommand() {
    private val input by option("--input").required()
    private val output by option("--output")

    override fun run() {
        val out = if (output != null) output else input.substring(0, input.lastIndexOf(".")) + ".ttl"
        BodsJsonToRdf.jsonlToRdf(File(input), File(out), RDFFormat.TURTLE)
    }
}
