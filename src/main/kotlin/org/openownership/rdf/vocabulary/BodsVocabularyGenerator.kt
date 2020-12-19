package org.openownership.rdf.vocabulary

import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.CsvRow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.openownership.rdf.resourceAsInput
import org.openownership.rdf.unzip
import org.slf4j.LoggerFactory
import java.io.File
import java.io.OutputStream
import java.io.StringReader
import java.util.*
import kotlin.streams.toList


/**
 * @author Cosmin Marginean
 */
object BodsVocabularyGenerator {

    private const val CODE = "code"
    private const val TITILE = "title"
    private const val DESCRIPTION = "description"

    private val log = LoggerFactory.getLogger(BodsVocabularyGenerator::class.java)
    private val httpClient = OkHttpClient()
    private val tempDir = File(System.getProperty("java.io.tmpdir"))

    fun writeVocabulary(bodsSchemaVersion: String, repository: Repository) {
        val model = generateVocabulary(bodsSchemaVersion)
        repository.connection.use { connection ->
            connection.add(model.stream().toList())
        }
    }

    fun writeVocabulary(bodsSchemaVersion: String, outputFile: File) {
        outputFile.outputStream().use { outputStream ->
            writeVocabulary(bodsSchemaVersion, outputStream)
        }
    }

    fun writeVocabulary(bodsSchemaVersion: String, outputStream: OutputStream) {
        val writer = Rio.createWriter(RDFFormat.TURTLE, outputStream)
        writer.startRDF()
        BodsRdf.addNamespaces(writer)
        generateVocabulary(bodsSchemaVersion)
                .stream()
                .forEach { statement -> writer.handleStatement(statement) }
        writer.endRDF()
    }

    fun generateVocabulary(bodsSchemaVersion: String): Model {
        val url = "https://github.com/openownership/data-standard/zipball/$bodsSchemaVersion"
        log.info("Downloading scheme release from $url")
        val httpRequest = Request.Builder()
                .url(url)
                .get()
                .build()
        val response = httpClient.newCall(httpRequest).execute()
        if (response.code != 200) {
            throw RuntimeException("Could not download branch zipball from $url")
        }

        // Unzip the release
        val tempZip = File(tempDir, UUID.randomUUID().toString())
        val tempUnzipDir = File(tempDir, UUID.randomUUID().toString())
        tempUnzipDir.mkdirs()
        tempZip.outputStream().use { os -> response.body!!.byteStream().copyTo(os) }
        unzip(tempZip, tempUnzipDir)
        val packageDir = tempUnzipDir
                .listFiles()
                .find { it.name.startsWith("openownership-data-standard") }!!

        val model = LinkedHashModel()
        addTopLevelDefinitions(model)
        addEntityTypes(packageDir, model)
        addInterestTypes(packageDir, model)
        return model
    }

    private fun addTopLevelDefinitions(model: Model) {
        val rdfParser = Rio.createParser(RDFFormat.TURTLE)
        rdfParser.setRDFHandler(StatementCollector(model))
        rdfParser.parse(resourceAsInput("vocabulary-base.ttl"), RepositoryConfigSchema.NAMESPACE)
    }

    private fun addEntityTypes(packageDir: File, model: Model) {
        csvRows(packageDir, "schema/codelists/entityType.csv")
                .forEach { row ->
                    val entityType = BodsRdf.entityType(row.getField(CODE)!!)
                    model.add(entityType, RDF.TYPE, RDFS.CLASS)
                    model.add(entityType, RDFS.SUBCLASSOF, BodsRdf.TYPE_ENTITY)
                    model.add(entityType, RDFS.LABEL, row.getField(TITILE)!!.literal())
                    model.add(entityType, RDFS.COMMENT, row.getField(DESCRIPTION)!!.literal())
                }
    }

    private fun addInterestTypes(packageDir: File, model: Model) {
        csvRows(packageDir, "schema/codelists/interestType.csv")
                .forEach { row ->
                    val interestType = BodsRdf.interestType(row.getField(CODE)!!)
                    model.add(interestType, RDF.TYPE, RDFS.CLASS)
                    model.add(interestType, RDFS.SUBCLASSOF, BodsRdf.TYPE_INTEREST)
                    model.add(interestType, RDFS.LABEL, row.getField(TITILE)!!.literal())
                    model.add(interestType, RDFS.COMMENT, (row.getField(DESCRIPTION) ?: "").literal())
                }
    }

    fun csvRows(packageDir: File, file: String): List<CsvRow> {
        val csvReader = CsvReader()
        csvReader.setTextDelimiter('"')
        csvReader.setContainsHeader(true)
        val content = File(packageDir, file)
                .readText()
                .replace(",\\s+\"".toRegex(), ",")
        return csvReader.read(StringReader(content)).rows
    }
}