# NVA BRAGE migration

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/5c93275b8bc74cda81d3872af3b2271d)](https://www.codacy.com/gh/BIBSYSDEV/nva-brage-migration/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BIBSYSDEV/nva-brage-migration&amp;utm_campaign=Badge_Grade)

This repository contains code for extracting metadata from Brage bundles.

## Documentation

Documentation of this application exits
on [Jira]( https://unit.atlassian.net/wiki/spaces/NVAP/pages/2571501733/Brage-NVA+migrerings+Applikasjon)

If the dublin_core.xml contains dcvalue with element="identifier" qualifier="cristin", it will be
skipped (and logged as error).

## Run the CLI tool

Read the manual:

```shell
java -jar build/libs/nva-brage-migration-1.0-SNAPSHOT-all.jar -h
```

Sample import localy:

With specified zipfiles:

```shell
java -jar build/libs/nva-brage-migration-1.0-SNAPSHOT-all.jar zipfile1.zip zipfile2.zip -c custommer-id
```

With all zipfiles specified by the samlingsfil.txt:

```shell
java -jar build/libs/nva-brage-migration-1.0-SNAPSHOT-all.jar -c custommer-id
```

Sample import from brage-instance:

It is possible to run the .jar file by sending customer value only, but only when running .jar
file on brage instance and archives are located in /brageexports/customer/
In this case archive will be unzipped at /brageexports/outputcustomer/

```shell
java -jar build/libs/nva-brage-migration-1.0-SNAPSHOT-all.jar -c custommer
```


## Run the CLI tool from brage instance

Sample import pushing resources to default bucket:

.jar file has to be placed into the root directory, and we have to specify directory with .zip bundles

Have to specify location with your JRE since default JRE is Java 8

When migrating same collection many times in row, remember to delete directory with unzipped bundles, otherwise log files will be affected.

```shell
/etc/alternatives/jre_11/bin/java -jar nva-brage-migration-1.1-all.jar -c NVE -a -D /brage/nve/app/export
```

# How to run Excel scraping:

-j flag can be set to "experimental" (sandbox, no lambda listening), "sandbox", "dev", "test", or "prod"

In build.gradle change main class to "no.sikt.nva.unis.ExcelScrapingCommand" and build.
```groovy
application {
    mainClass = "no.sikt.nva.unis.ExcelScrapingCommand"
}
```


```shell
java -jar build/libs/nva-brage-migration-1.2-all.jar -e ~/Desktop/unis/Metadata.xlsx -j experimental
```