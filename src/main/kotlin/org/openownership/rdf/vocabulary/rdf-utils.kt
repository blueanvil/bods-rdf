package org.openownership.rdf.vocabulary

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.vocabulary.XSD

/**
 * @author Cosmin Marginean
 */
fun String.literal(): Value = SimpleValueFactory.getInstance().createLiteral(this)
fun String.literalDate(): Value = SimpleValueFactory.getInstance().createLiteral(this, XSD.DATE)

fun rdfStatement(s: Resource, p: IRI, o: Value): Statement {
    return SimpleValueFactory.getInstance().createStatement(s, p, o)
}
