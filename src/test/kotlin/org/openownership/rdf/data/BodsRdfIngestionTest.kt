package org.openownership.rdf.data

import org.eclipse.rdf4j.model.impl.TreeModel
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.config.RepositoryConfig
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.manager.RepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.openownership.rdf.vocabulary.BodsVocabularyGenerator
import org.slf4j.LoggerFactory
import org.testng.annotations.Test
import java.io.File
import java.io.InputStream

/**
 * @author Cosmin Marginean
 */
class BodsRdfIngestionTest {

    @Test
    fun loadJsonl() {
        val repository = getRdfRepository("http://localhost:7200", "open-ownership", "admin", "admin")
        BodsVocabularyGenerator.writeVocabulary("0.2.0", repository)
        BodsDataLoader.loadJsonl(File("statements.latest.jsonl"), repository)
    }

    private fun getRdfRepository(connectionUrl: String,
                                 repositoryName: String,
                                 username: String,
                                 password: String): Repository {
        val repositoryManager = RemoteRepositoryManager(connectionUrl)
        repositoryManager.init()
        repositoryManager.setUsernameAndPassword(username, password)
        createIfNotPresent(repositoryManager, repositoryName)
        return repositoryManager.getRepository(repositoryName)
    }

    private fun createIfNotPresent(repositoryManager: RepositoryManager, repositoryName: String) {
        if (repositoryManager.hasRepositoryConfig(repositoryName)) {
            log.info("Repository $repositoryName exists, skipping.")
            return
        }

        log.info("Creating RDF repository $repositoryName")
        val repoConfigStatements = TreeModel()
        val rdfParser = Rio.createParser(RDFFormat.TURTLE)
        rdfParser.setRDFHandler(StatementCollector(repoConfigStatements))
        rdfParser.parse(resourceAsInput("graphdb-repository.ttl"), RepositoryConfigSchema.NAMESPACE)
        val repositoryConfig = RepositoryConfig.create(repoConfigStatements, null)
        repositoryManager.addRepositoryConfig(repositoryConfig)
    }

    fun resourceAsInput(classpathLocation: String): InputStream {
        val classLoader = Thread.currentThread().contextClassLoader
        return classLoader.getResourceAsStream(classpathLocation)
    }

    companion object {
        private val log = LoggerFactory.getLogger(BodsRdfIngestionTest::class.java)
    }
}
