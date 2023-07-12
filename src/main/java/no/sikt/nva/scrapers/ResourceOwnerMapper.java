package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import static no.sikt.nva.scrapers.CustomerMapper.BORA;
import static no.sikt.nva.scrapers.CustomerMapper.CMI;
import static no.sikt.nva.scrapers.CustomerMapper.DMMH;
import static no.sikt.nva.scrapers.CustomerMapper.FAFO;
import static no.sikt.nva.scrapers.CustomerMapper.FDIR;
import static no.sikt.nva.scrapers.CustomerMapper.FHS;
import static no.sikt.nva.scrapers.CustomerMapper.HIMOLDE;
import static no.sikt.nva.scrapers.CustomerMapper.HIOF;
import static no.sikt.nva.scrapers.CustomerMapper.HIVOLDA;
import static no.sikt.nva.scrapers.CustomerMapper.HVLOPEN;
import static no.sikt.nva.scrapers.CustomerMapper.KRISTIANIA;
import static no.sikt.nva.scrapers.CustomerMapper.KRUS;
import static no.sikt.nva.scrapers.CustomerMapper.NGI;
import static no.sikt.nva.scrapers.CustomerMapper.NHH;
import static no.sikt.nva.scrapers.CustomerMapper.NIBIO;
import static no.sikt.nva.scrapers.CustomerMapper.NIH;
import static no.sikt.nva.scrapers.CustomerMapper.NORD;
import static no.sikt.nva.scrapers.CustomerMapper.NR;
import static no.sikt.nva.scrapers.CustomerMapper.NTNU;
import static no.sikt.nva.scrapers.CustomerMapper.ODA;
import static no.sikt.nva.scrapers.CustomerMapper.SAMFORSK;
import static no.sikt.nva.scrapers.CustomerMapper.NVE;
import static no.sikt.nva.scrapers.CustomerMapper.UIA;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import no.sikt.nva.brage.migration.common.model.record.ResourceOwner;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class ResourceOwnerMapper {

    public static final String SANDBOX = "sandbox";
    public static final String DEVELOP = "dev";
    public static final String TEST = "test";
    public static final String PROD = "prod";
    public static final String NVE_OWNER_VALUE = "nve@5948.0.0.0";
    public static final String NVE_CRISTIN_IDENTIFIER = "5948.0.0.0";
    public static final String KRUS_OWNER_VALUE = "krus@1661.0.0.0";
    public static final String KRUS_CRISTIN_IDENTIFIER = "1661.0.0.0";
    public static final String KRISTIANIA_OWNER_VALUE = "kristiania@1615.0.0.0";
    public static final String KRISTIANIA_CRISTIN_IDENTIFIER = "1615.0.0.0";
    public static final String FHS_OWNER_VALUE = "fhs@1627.0.0.0";
    public static final String FHS_CRISTIN_IDENTIFIER = "1627.0.0.0";
    public static final String HIOF_OWNER_VALUE = "hiof@224.0.0.0";
    public static final String HIOF_CRISTIN_IDENTIFIER = "224.0.0.0";
    public static final String NGI_OWNER_VALUE = "ngi@7452.0.0.0";
    public static final String NGI_CRISTIN_IDENTIFIER = "7452.0.0.0";
    public static final String NIBIO_OWNER_VALUE = "nibio@7677.0.0.0";
    public static final String NIBIO_CRISTIN_IDENTIFIER = "7677.0.0.0";
    public static final String NIH_OWNER_VALUE = "nih@150.0.0.0";
    public static final String NIH_CRISTIN_IDENTIFIER = "150.0.0.0";
    public static final String NTNU_OWNER_VALUE = "ntnu@194.0.0.0";
    public static final String NTNU_CRISTIN_IDENTIFIER = "194.0.0.0";
    public static final String NR_OWNER_VALUE = "nr@7467.0.0.0";
    public static final String NR_CRISTIN_IDENTIFIER = "7467.0.0.0";
    public static final String SAMFORSK_OWNER_VALUE = "samforsk@7403.0.0.0";
    public static final String SAMFORSK_CRISTIN_IDENTIFIER = "7403.0.0.0";
    public static final String CMI_OWNER_VALUE = "cmi@7416.0.0.0";
    public static final String CMI_CRISTIN_IDENTIFIER = "7416.0.0.0";
    public static final String DMMH_OWNER_VALUE = "dmmh@253.0.0.0";
    public static final String DMMH_CRISTIN_IDENTIFIER = "253.0.0.0";
    public static final String FAFO_OWNER_VALUE = "fafo@7425.0.0.0";
    public static final String FAFO_CRISTIN_IDENTIFIER = "7425.0.0.0";
    public static final String FDIR_OWNER_VALUE = "fdir@5947.0.0.0";
    public static final String FDIR_CRISTIN_IDENTIFIER = "5947.0.0.0";
    public static final String UIA_OWNER_VALUE = "uia@201.0.0.0";
    public static final String UIA_CRISTIN_IDENTIFIER = "201.0.0.0";
    public static final String NORD_OWNER_VALUE = "nord@204.0.0.0";
    public static final String NORD_CRISTIN_IDENTIFIER = "204.0.0.0";
    public static final String ODA_OWNER_VALUE = "oda@novaluepresent";
    public static final String ODA_CRISTIN_IDENTIFIER = "novaluepresent";
    public static final String HIMOLDE_OWNER_VALUE = "himolde@211.0.0.0";
    public static final String HIMOLDE_CRISTIN_IDENTIFIER = "211.0.0.0";
    public static final String HIVOLDA_OWNER_VALUE = "hivolda@223.0.0.0";
    public static final String HIVOLDA_CRISTIN_IDENTIFIER = "223.0.0.0";
    public static final String HVLOPEN_OWNER_VALUE = "hvlopen@203.0.0.0";
    public static final String HVLOPEN_CRISTIN_IDENTIFIER = "203.0.0.0";
    public static final String NHH_OWNER_VALUE = "nhh@191.0.0.0";
    public static final String NHH_CRISTIN_IDENTIFIER = "191.0.0.0";
    public static final String BORA_OWNER_VALUE = "bora@184.0.0.0";
    public static final String BORA_CRISTIN_IDENTIFIER = "184.0.0.0";
    public static final String CRISTIN_IDENTIFIER = "CRISTIN_IDENTIFIER";
    public static final String OWNER_VALUE = "OWNER_VALUE";
    public static final String SANDBOX_URI = "https://api.sandbox.nva.aws.unit.no/cristin/organization/";
    public static final String DEVELOP_URI = "https://api.sandbox.nva.aws.unit.no/cristin/organization/";
    public static final String TEST_URI = "https://api.test.nva.aws.unit.no/cristin/organization/";
    public static final String PROD_URI = "https://api.nva.aws.unit.no/cristin/organization/";
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private static final Map<String, Map<String, String>> RESOURCE_OWNER_MAP = Map.ofEntries(
        entry(NVE, Map.ofEntries(entry(OWNER_VALUE, NVE_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, NVE_CRISTIN_IDENTIFIER))),
        entry(KRUS, Map.ofEntries(entry(OWNER_VALUE, KRUS_OWNER_VALUE),
                                  entry(CRISTIN_IDENTIFIER, KRUS_CRISTIN_IDENTIFIER))),
        entry(KRISTIANIA, Map.ofEntries(entry(OWNER_VALUE, KRISTIANIA_OWNER_VALUE),
                                        entry(CRISTIN_IDENTIFIER, KRISTIANIA_CRISTIN_IDENTIFIER))),
        entry(FHS, Map.ofEntries(entry(OWNER_VALUE, FHS_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, FHS_CRISTIN_IDENTIFIER))),
        entry(HIOF, Map.ofEntries(entry(OWNER_VALUE, HIOF_OWNER_VALUE),
                                  entry(CRISTIN_IDENTIFIER, HIOF_CRISTIN_IDENTIFIER))),
        entry(NGI, Map.ofEntries(entry(OWNER_VALUE, NGI_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, NGI_CRISTIN_IDENTIFIER))),
        entry(NIBIO, Map.ofEntries(entry(OWNER_VALUE, NIBIO_OWNER_VALUE),
                                   entry(CRISTIN_IDENTIFIER, NIBIO_CRISTIN_IDENTIFIER))),
        entry(NIH, Map.ofEntries(entry(OWNER_VALUE, NIH_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, NIH_CRISTIN_IDENTIFIER))),
        entry(NTNU, Map.ofEntries(entry(OWNER_VALUE, NTNU_OWNER_VALUE),
                                  entry(CRISTIN_IDENTIFIER, NTNU_CRISTIN_IDENTIFIER))),
        entry(NR, Map.ofEntries(entry(OWNER_VALUE, NR_OWNER_VALUE),
                                entry(CRISTIN_IDENTIFIER, NR_CRISTIN_IDENTIFIER))),
        entry(SAMFORSK, Map.ofEntries(entry(OWNER_VALUE, SAMFORSK_OWNER_VALUE),
                                entry(CRISTIN_IDENTIFIER, SAMFORSK_CRISTIN_IDENTIFIER))),
        entry(CMI, Map.ofEntries(entry(OWNER_VALUE, CMI_OWNER_VALUE),
                                      entry(CRISTIN_IDENTIFIER, CMI_CRISTIN_IDENTIFIER))),
        entry(DMMH, Map.ofEntries(entry(OWNER_VALUE, DMMH_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, DMMH_CRISTIN_IDENTIFIER))),
        entry(FAFO, Map.ofEntries(entry(OWNER_VALUE, FAFO_OWNER_VALUE),
                                  entry(CRISTIN_IDENTIFIER, FAFO_CRISTIN_IDENTIFIER))),
        entry(FDIR, Map.ofEntries(entry(OWNER_VALUE, FDIR_OWNER_VALUE),
                                  entry(CRISTIN_IDENTIFIER, FDIR_CRISTIN_IDENTIFIER))),
        entry(UIA, Map.ofEntries(entry(OWNER_VALUE, UIA_OWNER_VALUE),
                                  entry(CRISTIN_IDENTIFIER, UIA_CRISTIN_IDENTIFIER))),
        entry(NORD, Map.ofEntries(entry(OWNER_VALUE, NORD_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, NORD_CRISTIN_IDENTIFIER))),
        entry(ODA, Map.ofEntries(entry(OWNER_VALUE, ODA_OWNER_VALUE),
                                  entry(CRISTIN_IDENTIFIER, ODA_CRISTIN_IDENTIFIER))),
        entry(HIMOLDE, Map.ofEntries(entry(OWNER_VALUE, HIMOLDE_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, HIMOLDE_CRISTIN_IDENTIFIER))),
        entry(HIVOLDA, Map.ofEntries(entry(OWNER_VALUE, HIVOLDA_OWNER_VALUE),
                                     entry(CRISTIN_IDENTIFIER, HIVOLDA_CRISTIN_IDENTIFIER))),
        entry(HVLOPEN, Map.ofEntries(entry(OWNER_VALUE, HVLOPEN_OWNER_VALUE),
                                     entry(CRISTIN_IDENTIFIER, HVLOPEN_CRISTIN_IDENTIFIER))),
        entry(NHH, Map.ofEntries(entry(OWNER_VALUE, NHH_OWNER_VALUE),
                                     entry(CRISTIN_IDENTIFIER, NHH_CRISTIN_IDENTIFIER))),
        entry(BORA, Map.ofEntries(entry(OWNER_VALUE, BORA_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, BORA_CRISTIN_IDENTIFIER)))

    );

    private static final Map<String, String> ENVIRONMENT_URI_MAP = Map.ofEntries(
        entry(SANDBOX, SANDBOX_URI),
        entry(DEVELOP, DEVELOP_URI),
        entry(TEST, TEST_URI),
        entry(PROD, PROD_URI));

    public ResourceOwnerMapper() {
    }

    public ResourceOwner getResourceOwner(String customerShortName, String environment) {
        return Optional.ofNullable(customerShortName)
                   .map(customer -> RESOURCE_OWNER_MAP.get(customer.toLowerCase(Locale.ROOT)))
                   .map(valueMap -> constructResourceOwner(valueMap, environment))
                   .orElse(null);
    }

    private ResourceOwner constructResourceOwner(Map<String, String> valueMap, String environment) {
        return new ResourceOwner(valueMap.get(OWNER_VALUE), constructCristinOrganizationUri(valueMap, environment));
    }

    private URI constructCristinOrganizationUri(Map<String, String> valueMap, String environment) {
        return URI.create(ENVIRONMENT_URI_MAP.get(environment) + valueMap.get(CRISTIN_IDENTIFIER));
    }
}
