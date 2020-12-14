package org.openownership.rdf.vocabulary

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.*
import org.eclipse.rdf4j.rio.RDFWriter

/**
 * @author Cosmin Marginean
 */
object BodsRdf {
    const val NAMESPACE = "http://bods.openownership.org/vocabulary/"
    const val PREFIX = "bods"
    const val RES_NAMESPACE = "http://bods.openownership.org/resource/"
    const val RES_PREFIX = "bods-res"

    fun resource(value: String): IRI = SimpleValueFactory.getInstance().createIRI(RES_NAMESPACE, value)

    fun iri(value: String): IRI = SimpleValueFactory.getInstance().createIRI(NAMESPACE, value)

    fun entityType(bodsEntityType: String): IRI = iri(bodsEntityType.codeListToType())
    fun interestType(bodsInterestType: String): IRI = iri(bodsInterestType.codeListToType())

    val TYPE_PARTY = iri("Party")
    val TYPE_PERSON = iri("Person")
    val TYPE_ENTITY = iri("Entity")
    val TYPE_STATEMENT = iri("Statement")
    val TYPE_INTEREST = iri("Interest")

    val PROP_INTERESTED_PARTY = iri("hasInterestedParty")
    val PROP_SUBJECT = iri("hasSubject")
    val PROP_STATES_INTEREST = iri("statesInterest")
    val PROP_SUBJECT_ID = iri("statementId")
    val PROP_STATEMENT_DATE = iri("statementDate")
    val PROP_STATEMENT_ID = iri("statementId")
    val PROP_SOURCE_TYPE = iri("sourceType")
    val PROP_START_DATE = iri("startDate")
    val PROP_END_DATE = iri("endDate")

    val REQUIRED_NAMESPACES = mapOf(
            RDF.PREFIX to RDF.NAMESPACE,
            RDFS.PREFIX to RDFS.NAMESPACE,
            OWL.PREFIX to OWL.NAMESPACE,
            XSD.PREFIX to XSD.NAMESPACE,
            FOAF.PREFIX to FOAF.NAMESPACE,
            PREFIX to NAMESPACE,
            RES_PREFIX to RES_NAMESPACE
    )

    fun addNamespaces(writer: RDFWriter) {
        REQUIRED_NAMESPACES
                .forEach { writer.handleNamespace(it.key, it.value) }
    }
}

private fun String.codeListToType(): String {
    return split("-")
            .map { it.capitalize() }
            .joinToString("")
}