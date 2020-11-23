package org.openownership.rdf.vocabulary

import org.eclipse.rdf4j.model.*
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.XSD
import org.eclipse.rdf4j.rio.RDFWriter

/**
 * @author Cosmin Marginean
 */
fun String.literal(): Value = SimpleValueFactory.getInstance().createLiteral(this)
fun literalDate(date: String): Value = SimpleValueFactory.getInstance().createLiteral(date, XSD.DATE)

fun rdfStatement(s: Resource, p: IRI, o: Value): Statement {
    return SimpleValueFactory.getInstance().createStatement(s, p, o)
}

fun rdfStatement(s: Resource, p: IRI, o: Value,
                 annotationType: IRI, annotationValue: Value): Statement {
    val triple = SimpleValueFactory.getInstance().createTriple(s, p, o)
    return rdfStatement(triple, annotationType, annotationValue)
}

fun Model.add(s: Resource, p: IRI, o: Value) {
    add(rdfStatement(s, p, o))
}

fun RDFWriter.write(s: Resource, p: IRI, o: Value) {
    handleStatement(rdfStatement(s, p, o))
}
