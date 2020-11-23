package org.openownership.rdf.vocabulary

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.impl.SimpleValueFactory

/**
 * @author Cosmin Marginean
 */
object BodsResource {
    const val NAMESPACE = "http://bods.openownership.org/resource/"
    const val PREFIX = "bods-res"

    fun iri(value: String): IRI = SimpleValueFactory.getInstance().createIRI(NAMESPACE, value)
}
