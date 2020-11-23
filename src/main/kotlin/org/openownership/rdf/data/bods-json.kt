package org.openownership.rdf.data

import com.beust.klaxon.JsonObject
import org.eclipse.rdf4j.model.IRI
import org.openownership.rdf.vocabulary.BodsVocabulary

/**
 * @author Cosmin Marginean
 */
internal fun JsonObject.isEntity(): Boolean = string("statementType") == "entityStatement"
internal fun JsonObject.isPerson(): Boolean = string("statementType") == "personStatement"
internal fun JsonObject.isOwnershipCtrl(): Boolean = string("statementType") == "ownershipOrControlStatement"
internal fun JsonObject.statementId(): String = string("statementID")!!
internal fun JsonObject.entityType(): IRI = BodsVocabulary.iri(string("entityType")!!.capitalize())
internal fun JsonObject.subjectId(): String = obj("subject")!!.string("describedByEntityStatement")!!
internal fun JsonObject.statementDate(): String = string("statementDate")!!
internal fun JsonObject.sourceType(): String = obj("source")?.array<String>("type")?.joinToString(";") ?: ""

internal fun JsonObject.interestedPartyId(): String? {
    val ip = obj("interestedParty")
    return when {
        ip == null -> null
        ip.containsKey("describedByEntityStatement") -> ip.string("describedByEntityStatement")!!
        ip.containsKey("describedByPersonStatement") -> ip.string("describedByPersonStatement")!!
        else -> {
            null
        }
    }
}

internal fun JsonObject.name(): String {
    return if (containsKey("name")) {
        string("name")!!
    } else {
        array<JsonObject>("names")?.firstOrNull()?.string("fullName") ?: "UNKNOWN"
    }
}
