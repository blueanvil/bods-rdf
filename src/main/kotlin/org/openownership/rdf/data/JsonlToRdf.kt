package org.openownership.rdf.data

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.eclipse.rdf4j.model.BNode
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.FOAF
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.openownership.rdf.BUFFER_1MB
import org.openownership.rdf.vocabulary.BodsRdf
import org.openownership.rdf.vocabulary.literal
import org.openownership.rdf.vocabulary.rdfStatement
import org.slf4j.LoggerFactory
import java.io.File

/**
 * @author Cosmin Marginean
 */
object JsonlToRdf {

    private val log = LoggerFactory.getLogger(JsonlToRdf::class.java)

    fun jsonlToRdf(jsonlFile: File, statementHandler: (Statement) -> Unit) {
        log.info("Loading JSONL file $jsonlFile")
        val jsonParser = Parser.default()
        var count = 0L
        var withNoInterestedParty = 0L

        jsonlFile.bufferedReader(bufferSize = BUFFER_1MB).useLines { lines ->
            lines.forEach { line ->
                val statement = jsonParser.parse(StringBuilder(line)) as JsonObject
                val statementId = statement.statementId()
                count++

                when {
                    statement.isEntity() -> processEntity(statementHandler, statement)
                    statement.isPerson() -> processPerson(statementHandler, statement)
                    statement.isOwnershipCtrl() -> {
                        val interestedPartyId = statement.interestedPartyId()
                        processInterests(statement, interestedPartyId, statementHandler)
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

    fun processInterests(statement: JsonObject, interestedPartyId: String?, handler: (Statement) -> Unit) {
        val entity = BodsRdf.resource(statement.subjectId())
        val interestedParty = if (interestedPartyId != null) BodsRdf.resource(interestedPartyId) else SimpleValueFactory.getInstance().createBNode()
        val statementId = statement.statementId()
        val statementObject = BodsRdf.resource(statementId)
        handler(rdfStatement(statementObject, RDF.TYPE, BodsRdf.TYPE_STATEMENT))
        handler(rdfStatement(statementObject, BodsRdf.PROP_INTERESTED_PARTY, interestedParty))
        handler(rdfStatement(statementObject, BodsRdf.PROP_SUBJECT, entity))
        handler(rdfStatement(statementObject, BodsRdf.PROP_STATEMENT_ID, statementId.literal()))
        statement.array<JsonObject>("interests")?.forEachIndexed { index, interest ->
            val interestType = BodsRdf.interestType(interest.string("type")!!)
            val interestObject = BodsRdf.resource("${statementId}_$index")
            handler(rdfStatement(interestObject, RDF.TYPE, interestType))
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
