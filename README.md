# An RDF vocabulary for the BODS JSON Schema
Please read our detailed proposal for the BODS RDF Vocabulary: https://docs.google.com/document/d/1gZVPBXfqC72WHPBEkWhzg4Cik8FtHQhfG3SPcnvFn3c

## RDF Vocabulary definitions
* https://github.com/blueanvil/bods-rdf/blob/main/vocabulary/openownership-vocabulary-0.1.0.ttl
* https://github.com/blueanvil/bods-rdf/blob/main/vocabulary/openownership-vocabulary-0.2.0.ttl

## Generating the BODS RDF vocabulary
The BODS vocabulary can be generated in a Turtle (.ttl) format from BODS JSON schema.
The `--schemaVersion` argument is required, and it points to a schema [release branch](https://github.com/openownership/data-standard/branches).
This will download a zipball for the schema version branch and, by default, it will generate the vocabulary in a file
`vocabulary/openownership-vocabulary-${schemaVersion}.ttl`. To specify a different location for the output, use the `--output` argument:
```
gradle vocabulary --args="--schemaVersion=0.2.0"
gradle vocabulary --args="--schemaVersion=0.2.0 --output=my-vocabulary.ttl"
```

## Converting BODS JSON data to RDF
This tool converts BODS JSON data to the Turtle* (.ttls) format. Other formats will be considered in the future, but given
the nature of this data (large, and requires streaming), the .ttls format is one of the most compact and suitable for this purpose.
If no output file is specified (using `--output`), the system will generate a file with the same path and name, but with the extension replaced to `.ttls`.
```
gradle jsonlToRdf --args="--input=statements.latest.jsonl"
gradle jsonlToRdf --args="--input=statements.latest.jsonl --output=my-data.ttls"
```

## SPARQL Samples
Please check the `sparql-examples` directory for the complete list of working SPARQL queries and some sample output. 

## Ingesting BODS JSON data into an RDF repository
The test `BodsRdfIngestionTest` is an example written in Kotlin which converts BODS JSON data and ingests it
into an RDF repository (using [GraphDB](https://www.ontotext.com/products/graphdb/)).
