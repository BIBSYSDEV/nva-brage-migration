# NVA BRAGE migration

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/5c93275b8bc74cda81d3872af3b2271d)](https://www.codacy.com/gh/BIBSYSDEV/nva-brage-migration/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BIBSYSDEV/nva-brage-migration&amp;utm_campaign=Badge_Grade)

This repository contains code for extracting metadata from Brage bundles.

## This tool will not convert publications containing cristin-id or publications that are clausuled

If the dublin_core.xml contains dcvalue with element="identifier" qualifier="cristin", it will be
skipped (and logged as error).

## Run the CLI tool

Read the manual:

```shell
java -jar build/libs/nva-brage-migration-1.0-SNAPSHOT.jar -h
```

Sample import:

```shell
java -jar build/libs/nva-brage-migration-1.0-SNAPSHOT.jar -z "zipfile1.zip zipfile2.zip" -c custommer-id
```
