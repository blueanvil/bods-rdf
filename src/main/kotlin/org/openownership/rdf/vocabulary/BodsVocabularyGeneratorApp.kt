package org.openownership.rdf.vocabulary

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import java.io.File

/**
 * @author Cosmin Marginean
 */
object BodsVocabularyGeneratorApp {

    @JvmStatic
    fun main(args: Array<String>) {
        BodsVocabularyGeneratorRunner().main(args)
    }
}

class BodsVocabularyGeneratorRunner : CliktCommand() {
    private val schemaVersion by option("--schemaVersion").required()
    private val outputFilePath by option("--output")

    override fun run() {
        val output = if (outputFilePath != null) outputFilePath else "vocabulary/openownership-vocabulary-$schemaVersion.ttl"
        BodsVocabularyGenerator.writeVocabulary(schemaVersion, File(output))
    }
}
