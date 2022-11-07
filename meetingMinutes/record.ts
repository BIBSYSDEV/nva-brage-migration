interface Record {
  origin: String;  //plassering til ressursen i den opprinnelige filstrukturen
  embargo: String;
  customerId: String;   //kunde id, for eksempel NVE
  rightsholder: String;
  spatialCoverage: String; //
  publisherAuthority: Boolean;
  id: URL;  //handle
  doi: URL;
  type: Type; //brage type og nva type
  date: Date;
  language: Language;  //brage og nva språk
  entityDescription: EntityDescription;
  publication: Publication; //publikasjonsinformasjon
  contentBundle: ResourceContent; //filer knyttet til en ressurs
}

interface EntityDescription {
  descriptions: String[];  //beskrivelser
  abstracts: String[];
  mainTitle: String;
  alternativeTitles: String[];
  tags: String[];
  contributors: Contributor[]; //bidragsyter
  publicationInstance: PublicationInstance[];
}

interface Type {
  brage: String[];
  nva: String;
}

interface Language {
  brage: String;
  nva: URL;
}

interface Publication {
  journal: String;
  issn: String;
  isbn: String;
  publisher: String;
  partOfSeries: String;
  id: String;
}

interface ResourceContent {
  contentFiles: ContentFile[];
}

interface Contributor {
  type: String;
  role: String;
  brageRole: String;
  identity: Identity;
}

interface PublicationInstance {
  volume: String;  //antall sider
  issue: String;
  pageNumber: String;  //fra-til side
}

interface ContentFile {
  filename: String;
  bundleType: String;
  description: String;
  unknownType: String;
  identifier: String;
  license: License;
}

interface Identity {
  type: String;
  name: String;
}

interface License {
  brageLicense: String;
  NvaLicense: String;
}

interface Date {
  brage: String;
  nva: String;
}