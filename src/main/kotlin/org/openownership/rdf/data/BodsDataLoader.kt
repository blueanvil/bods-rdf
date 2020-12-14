package org.openownership.rdf.data

import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.openownership.rdf.vocabulary.BodsRdf
import java.io.File
import java.io.OutputStream

/**
 * @author Cosmin Marginean
 */
object BodsDataLoader {

    private const val STATEMENT_BATCH_SIZE = 10_000

    fun loadJsonl(jsonlFile: File, rdfRepository: Repository) {
        rdfRepository.connection.use { connection ->
            val statementsBatch = mutableListOf<Statement>()
            JsonlToRdf.jsonlToRdf(jsonlFile) { statement ->
                statementsBatch.add(statement)
                if (statementsBatch.size == STATEMENT_BATCH_SIZE) {
                    connection.add(statementsBatch)
                    statementsBatch.clear()
                }
            }
            if (statementsBatch.isNotEmpty()) {
                connection.add(statementsBatch)
            }
        }
    }

    fun jsonlToRdf(jsonlFile: File, outputFile: File) {
        outputFile.outputStream().use { outputStream ->
            jsonlToRdf(jsonlFile, outputStream, RDFFormat.TURTLESTAR)
        }
    }

    fun jsonlToRdf(jsonlFile: File, outputStream: OutputStream, format: RDFFormat) {
        val writer = Rio.createWriter(format, outputStream)
        writer.startRDF()
        BodsRdf.addNamespaces(writer)

        JsonlToRdf.jsonlToRdf(jsonlFile) { statement ->
            writer.handleStatement(statement)
        }

        writer.endRDF()
    }
}