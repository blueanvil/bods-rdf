# An RDF vocabulary for the BODS JSON Schema

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

