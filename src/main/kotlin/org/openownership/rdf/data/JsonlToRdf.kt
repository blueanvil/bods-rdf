package org.openownership.rdf.data

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.vocabulary.FOAF
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.openownership.rdf.BUFFER_1MB
import org.openownership.rdf.vocabulary.*
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
                val statementRes = BodsResource.iri(statementId)
                count++

                when {
                    statement.isEntity() -> processEntity(statementHandler, statementRes, statement)
                    statement.isPerson() -> processPerson(statementHandler, statementRes, statement)
                    statement.isOwnershipCtrl() -> {
                        val interestedPartyId = statement.interestedPartyId()
                        if (interestedPartyId != null) {
                            processInterests(statement, interestedPartyId, statementHandler)
                        } else {
                            log.warn("Statement $statementId has no interested party")
                            withNoInterestedParty++
                        }
                    }
                }

                if (count % 1_000_000 == 0L) log.info("Processed $count BODS statements")
            }
        }

        log.info("Finished processing $count BODS statements, $withNoInterestedParty with no interested party (ignored)")
    }

    fun processInterests(statement: JsonObject, interestedPartyId: String, statementHandler: (Statement) -> Unit) {
        val entity = BodsResource.iri(statement.subjectId())
        val ip = BodsResource.iri(interestedPartyId)
        val annotationStId = statement.statementId().literal()
        statement.array<JsonObject>("interests")?.forEach { interest ->
            val interestType = BodsVocabulary.interestType(interest.string("type")!!)
            statementHandler(rdfStatement(ip, interestType, entity))
            statementHandler(rdfStatement(ip, interestType, entity, BodsVocabulary.ANNOTATION_STATEMENT, annotationStId))
            statementHandler(rdfStatement(ip, interestType, entity, BodsVocabulary.ANNOTATION_STATEMENT_DATE, literalDate(statement.statementDate())))
            statementHandler(rdfStatement(ip, interestType, entity, BodsVocabulary.ANNOTATION_STATEMENT_SRC_TYPE, statement.sourceType().literal()))
        }
    }

    private fun processPerson(statementHandler: (Statement) -> Unit, statementRes: IRI, statement: JsonObject) {
        statementHandler(rdfStatement(statementRes, RDF.TYPE, BodsVocabulary.PERSON))
        statementHandler(rdfStatement(statementRes, FOAF.NAME, statement.name().literal()))
    }

    private fun processEntity(statementHandler: (Statement) -> Unit, statementResource: IRI, statement: JsonObject) {
        statementHandler(rdfStatement(statementResource, RDF.TYPE, statement.entityType()))
        statementHandler(rdfStatement(statementResource, FOAF.NAME, statement.name().literal()))
    }
}
