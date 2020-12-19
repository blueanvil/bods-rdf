package org.openownership.rdf.data

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.FOAF
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.openownership.rdf.BUFFER_1MB
import org.openownership.rdf.vocabulary.BodsRdf
import org.openownership.rdf.vocabulary.literal
import org.openownership.rdf.vocabulary.literalDate
import org.openownership.rdf.vocabulary.rdfStatement
import org.slf4j.LoggerFactory
import java.io.File
import java.io.OutputStream

/**
 * @author Cosmin Marginean
 */
object BodsJsonToRdf {

    private val log = LoggerFactory.getLogger(BodsJsonToRdf::class.java)

    private const val STATEMENT_BATCH_SIZE = 100_000

    fun loadJsonl(jsonlFile: File,
                  rdfRepository: Repository,
                  addHasInterestRelationship: Boolean = true) {
        rdfRepository.connection.use { connection ->
            val statementsBatch = mutableListOf<Statement>()
            jsonlToRdf(jsonlFile, addHasInterestRelationship) { statement ->
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

    fun jsonlToRdf(jsonlFile: File,
                   outputFile: File,
                   format: RDFFormat,
                   addHasInterestRelationship: Boolean = true) {
        outputFile.outputStream().use { outputStream ->
            jsonlToRdf(jsonlFile, outputStream, format, addHasInterestRelationship)
        }
    }

    fun jsonlToRdf(jsonlFile: File,
                   outputStream: OutputStream,
                   format: RDFFormat,
                   addHasInterestRelationship: Boolean = true) {
        val writer = Rio.createWriter(format, outputStream)
        writer.startRDF()
        BodsRdf.addNamespaces(writer)

        jsonlToRdf(jsonlFile, addHasInterestRelationship) { statement ->
            writer.handleStatement(statement)
        }

        writer.endRDF()
    }

    private fun jsonlToRdf(jsonlFile: File,
                           addHasInterestRelationship: Boolean,
                           statementHandler: (Statement) -> Unit) {
        log.info("Loading JSONL file $jsonlFile")
        val jsonParser = Parser.default()
        var count = 0L
        var withNoInterestedParty = 0L

        jsonlFile.bufferedReader(bufferSize = BUFFER_1MB).useLines { lines ->
            lines.forEach { line ->
                val statement = jsonParser.parse(StringBuilder(line)) as JsonObject
                count++

                when {
                    statement.isEntity() -> processEntity(statementHandler, statement)
                    statement.isPerson() -> processPerson(statementHandler, statement)
                    statement.isOwnershipCtrl() -> {
                        val interestedPartyId = statement.interestedPartyId()
                        processInterests(statement, addHasInterestRelationship, interestedPartyId, statementHandler)
                        if (interestedPartyId == null) {
                            withNoInterestedParty++
                        }
                    }
                }

                if (count % 1_000_000 == 0L) {
                    log.info("Processed $count BODS statements")
                }
            }
        }

        log.info("Finished processing $count BODS statements, $withNoInterestedParty with no interested party (ignored)")
    }

    private fun processInterests(statement: JsonObject,
                                 addHasInterestRelationship: Boolean,
                                 interestedPartyId: String?,
                                 handler: (Statement) -> Unit) {
        val entity = BodsRdf.resource(statement.subjectId())
        val interestedParty = if (interestedPartyId != null) BodsRdf.resource(interestedPartyId) else SimpleValueFactory.getInstance().createBNode()
        val statementId = statement.statementId()
        val statementObject = BodsRdf.resource(statementId)

        handler(rdfStatement(statementObject, RDF.TYPE, BodsRdf.TYPE_STATEMENT))
        handler(rdfStatement(statementObject, BodsRdf.PROP_INTERESTED_PARTY, interestedParty))
        handler(rdfStatement(statementObject, BodsRdf.PROP_SUBJECT, entity))
        handler(rdfStatement(statementObject, BodsRdf.PROP_STATEMENT_ID, statementId.literal()))
        if (addHasInterestRelationship) {
            handler(rdfStatement(interestedParty, BodsRdf.PROP_HAS_INTEREST_IN, entity))
        }

        val statementDate = statement.statementDate()
        if (statementDate != null) {
            handler(rdfStatement(statementObject, BodsRdf.PROP_STATEMENT_DATE, statementDate.literal()))
        }

        handler(rdfStatement(statementObject, BodsRdf.PROP_STATEMENT_SOURCE_TYPE, statement.sourceType().literal()))

        statement.array<JsonObject>("interests")?.forEachIndexed { index, interest ->
            val interestType = BodsRdf.interestType(interest.string("type")!!)
            val interestObject = BodsRdf.resource("${statementId}_$index")
            handler(rdfStatement(interestObject, RDF.TYPE, interestType))
            val startDate = interest.startDate()
            if (startDate != null) {
                handler(rdfStatement(interestObject, BodsRdf.PROP_INTEREST_START_DATE, startDate.literalDate()))
            }
            val endDate = interest.endDate()
            if (endDate != null) {
                handler(rdfStatement(interestObject, BodsRdf.PROP_INTEREST_END_DATE, endDate.literalDate()))
            }
            handler(rdfStatement(statementObject, BodsRdf.PROP_STATES_INTEREST, interestObject))
        }
    }

    private fun processPerson(statementHandler: (Statement) -> Unit, statement: JsonObject) {
        val statementRes = BodsRdf.resource(statement.statementId())
        statementHandler(rdfStatement(statementRes, RDF.TYPE, BodsRdf.TYPE_PERSON))
        statementHandler(rdfStatement(statementRes, FOAF.NAME, statement.name().literal()))
    }

    private fun processEntity(statementHandler: (Statement) -> Unit, statement: JsonObject) {
        val statementRes = BodsRdf.resource(statement.statementId())
        statementHandler(rdfStatement(statementRes, RDF.TYPE, statement.entityType()))
        statementHandler(rdfStatement(statementRes, FOAF.NAME, statement.name().literal()))
    }
}
