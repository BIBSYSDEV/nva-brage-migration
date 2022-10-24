interface Records {
  customerId: string, // I dette tilfellet NVE customer URI i NVA.
  id: string, //Handle URI. Verifisering vha. regex.
  doi: string // verifisert vha. regex.
  bareOrigin: string, // Path til lokalisasjon på dublinCore i datasettet. Brukes av utviklerne
  type: {
    brage: string[],
    nva: string, // mapped from brage type
  }
  date: {
    brage: string, // som regel kun årstall, men kan også inneholde månedsnummer
    nva: string, // årstall -> årstall. årstall + måned -> årstall-måned-første-dag
  }
  language: {
    brage: string, // Språk kode på.
    nva: string, //Mapped til språk URI.
  }
  license: string, // Hos Brage en URI, hos NVA en string enum.
  publisherAuthority: boolean,
  publication: Publication,
  entitityDescription: EntityDescription
}

interface EntityDescription {
  abstract: string[],
  description: string[],
  mainTitle: string,
  alternativeTitles: string[],
  contributors: [{
    type: string, //mappet til NVA type.
    role: string,
    identity: {
      type: string, //alltid "identity"
      name: string,
    }
  }],
  tags: string[],
}

interface Publication {
  issn: string, //Valideres offline
  isbn: string, // Validert vha. ekstern bibliotek.
  publisher: string,
  partOfSeries: string,
}