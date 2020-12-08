package org.openownership.rdf.vocabulary

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.*
import org.eclipse.rdf4j.rio.RDFWriter

/**
 * @author Cosmin Marginean
 */
object BodsVocabulary {
    const val NAMESPACE = "http://bods.openownership.org/vocabulary/"
    const val PREFIX = "bods"

    fun iri(value: String): IRI = SimpleValueFactory.getInstance().createIRI(NAMESPACE, value)

    fun entityType(bodsEntityType: String): IRI = iri(bodsEntityType.capitalize())
    fun interestType(bodsInterestType: String): IRI = iri(bodsInterestType.split("-")
            .mapIndexed { index, it -> if (index > 0) it.capitalize() else it }
            .joinToString(""))

    val HAS_INTEREST_IN = iri("hasInterestIn")
    val PARTY = iri("Party")
    val PERSON = iri("Person")
    val ENTITY = iri("Entity")
    val ANNOTATION_STATEMENT = iri("bodsStatementId")
    val ANNOTATION_STATEMENT_DATE = iri("bodsStatementDate")
    val ANNOTATION_STATEMENT_SRC_TYPE = iri("bodsStatementSourceType")

    val REQUIRED_NAMESPACES = mapOf(
            RDF.PREFIX to RDF.NAMESPACE,
            RDFS.PREFIX to RDFS.NAMESPACE,
            OWL.PREFIX to OWL.NAMESPACE,
            XSD.PREFIX to XSD.NAMESPACE,
            FOAF.PREFIX to FOAF.NAMESPACE,
            BodsVocabulary.PREFIX to BodsVocabulary.NAMESPACE,
            BodsResource.PREFIX to BodsResource.NAMESPACE
    )

    fun addNamespaces(writer: RDFWriter) {
        REQUIRED_NAMESPACES
                .forEach { writer.handleNamespace(it.key, it.value) }
    }
}
