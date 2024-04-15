package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import static no.sikt.nva.scrapers.CustomerMapper.AHO;
import static no.sikt.nva.scrapers.CustomerMapper.BANENOR;
import static no.sikt.nva.scrapers.CustomerMapper.BI;
import static no.sikt.nva.scrapers.CustomerMapper.BORA;
import static no.sikt.nva.scrapers.CustomerMapper.CICERO;
import static no.sikt.nva.scrapers.CustomerMapper.CMI;
import static no.sikt.nva.scrapers.CustomerMapper.DMMH;
import static no.sikt.nva.scrapers.CustomerMapper.FAFO;
import static no.sikt.nva.scrapers.CustomerMapper.FDIR;
import static no.sikt.nva.scrapers.CustomerMapper.FFI;
import static no.sikt.nva.scrapers.CustomerMapper.FHI;
import static no.sikt.nva.scrapers.CustomerMapper.FHS;
import static no.sikt.nva.scrapers.CustomerMapper.FNI;
import static no.sikt.nva.scrapers.CustomerMapper.HIMOLDE;
import static no.sikt.nva.scrapers.CustomerMapper.HIOF;
import static no.sikt.nva.scrapers.CustomerMapper.HIVOLDA;
import static no.sikt.nva.scrapers.CustomerMapper.HVLOPEN;
import static no.sikt.nva.scrapers.CustomerMapper.IFE;
import static no.sikt.nva.scrapers.CustomerMapper.IMR;
import static no.sikt.nva.scrapers.CustomerMapper.INN;
import static no.sikt.nva.scrapers.CustomerMapper.KHIO;
import static no.sikt.nva.scrapers.CustomerMapper.KRISTIANIA;
import static no.sikt.nva.scrapers.CustomerMapper.KRUS;
import static no.sikt.nva.scrapers.CustomerMapper.LDH;
import static no.sikt.nva.scrapers.CustomerMapper.MF;
import static no.sikt.nva.scrapers.CustomerMapper.NASJONALMUSEET;
import static no.sikt.nva.scrapers.CustomerMapper.NB;
import static no.sikt.nva.scrapers.CustomerMapper.NFORSK;
import static no.sikt.nva.scrapers.CustomerMapper.NGI;
import static no.sikt.nva.scrapers.CustomerMapper.NGU;
import static no.sikt.nva.scrapers.CustomerMapper.NHH;
import static no.sikt.nva.scrapers.CustomerMapper.NIBIO;
import static no.sikt.nva.scrapers.CustomerMapper.NIFU;
import static no.sikt.nva.scrapers.CustomerMapper.NIH;
import static no.sikt.nva.scrapers.CustomerMapper.NIKU;
import static no.sikt.nva.scrapers.CustomerMapper.NILU;
import static no.sikt.nva.scrapers.CustomerMapper.NINA;
import static no.sikt.nva.scrapers.CustomerMapper.NIVA;
import static no.sikt.nva.scrapers.CustomerMapper.NLA;
import static no.sikt.nva.scrapers.CustomerMapper.NMBU;
import static no.sikt.nva.scrapers.CustomerMapper.NMH;
import static no.sikt.nva.scrapers.CustomerMapper.NOFIMA;
import static no.sikt.nva.scrapers.CustomerMapper.NORCERESEARCH;
import static no.sikt.nva.scrapers.CustomerMapper.NORD;
import static no.sikt.nva.scrapers.CustomerMapper.NORGES_BANK;
import static no.sikt.nva.scrapers.CustomerMapper.NORSKFOLKEMUSEUM;
import static no.sikt.nva.scrapers.CustomerMapper.NPOLAR;
import static no.sikt.nva.scrapers.CustomerMapper.NR;
import static no.sikt.nva.scrapers.CustomerMapper.NTNU;
import static no.sikt.nva.scrapers.CustomerMapper.NUPI;
import static no.sikt.nva.scrapers.CustomerMapper.NVE;
import static no.sikt.nva.scrapers.CustomerMapper.ODA;
import static no.sikt.nva.scrapers.CustomerMapper.RA;
import static no.sikt.nva.scrapers.CustomerMapper.PHS;
import static no.sikt.nva.scrapers.CustomerMapper.RURALIS;
import static no.sikt.nva.scrapers.CustomerMapper.R_BUP;
import static no.sikt.nva.scrapers.CustomerMapper.SAMAS;
import static no.sikt.nva.scrapers.CustomerMapper.SAMFORSK;
import static no.sikt.nva.scrapers.CustomerMapper.SAMFUNNSFORSKNING;
import static no.sikt.nva.scrapers.CustomerMapper.SIHF;
import static no.sikt.nva.scrapers.CustomerMapper.SINTEF;
import static no.sikt.nva.scrapers.CustomerMapper.SSB;
import static no.sikt.nva.scrapers.CustomerMapper.STAMI;
import static no.sikt.nva.scrapers.CustomerMapper.STATPED;
import static no.sikt.nva.scrapers.CustomerMapper.STEINERHOYSKOLEN;
import static no.sikt.nva.scrapers.CustomerMapper.TOI;
import static no.sikt.nva.scrapers.CustomerMapper.UIA;
import static no.sikt.nva.scrapers.CustomerMapper.UIO;
import static no.sikt.nva.scrapers.CustomerMapper.UIS;
import static no.sikt.nva.scrapers.CustomerMapper.UIT;
import static no.sikt.nva.scrapers.CustomerMapper.UNIT;
import static no.sikt.nva.scrapers.CustomerMapper.USN;
import static no.sikt.nva.scrapers.CustomerMapper.VEGVESEN;
import static no.sikt.nva.scrapers.CustomerMapper.VID;
import static no.sikt.nva.scrapers.CustomerMapper.VETINST;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import java.util.concurrent.ConcurrentHashMap;
import no.sikt.nva.brage.migration.common.model.record.ResourceOwner;

@SuppressWarnings({"PMD.AvoidUsingHardCodedIP", "PMD.DoubleBraceInitialization"})
public class ResourceOwnerMapper {

    public static final String SANDBOX = "sandbox";
    public static final String DEVELOP = "dev";
    public static final String TEST = "test";
    public static final String PROD = "prod";
    public static final String NORGES_BANK_OWNER_VALUE = "norges-bank@5923.0.0.0";
    public static final String NORGES_BANK_CRISTIN_IDENTIFIER = "5923.0.0.0";
    public static final String RA_OWNER_VALUE = "ra@5989.0.0.0";
    public static final String RA_CRISTIN_IDENTIFIER = "5989.0.0.0";
    public static final String VID_OWNER_VALUE = "vid@251.0.0.0";
    public static final String VID_CRISTIN_IDENTIFIER = "251.0.0.0";
    public static final String BI_OWNER_VALUE = "bi@158.0.0.0";
    public static final String BI_CRISTIN_IDENTIFIER = "158.0.0.0";
    public static final String INN_OWNER_VALUE = "inn@209.0.0.0";
    public static final String INN_CRISTIN_IDENTIFIER = "209.0.0.0";
    public static final String NUPI_OWNER_VALUE = "nupi@7471.0.0.0";
    public static final String NUPI_CRISTIN_IDENTIFIER = "7471.0.0.0";
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
    public static final String ODA_OWNER_VALUE = "oda@215.0.0.0";
    public static final String ODA_CRISTIN_IDENTIFIER = "215.0.0.0";
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
    public static final String AHO_OWNER_VALUE = "aho@189.0.0.0";
    public static final String AHO_CRISTIN_IDENTIFIER = "189.0.0.0";
    public static final String IFE_OWNER_VALUE = "ife@7492.0.0.0";
    public static final String IFE_CRISTIN_IDENTIFIER = "7492.0.0.0";
    public static final String KHIO_OWNER_VALUE = "khio@260.0.0.0";
    public static final String KHIO_CRISTIN_IDENTIFIER = "260.0.0.0";
    public static final String LDH_OWNER_VALUE = "ldh@230.0.0.0";
    public static final String LDH_CRISTIN_IDENTIFIER = "230.0.0.0";
    public static final String MF_OWNER_VALUE = "mf@190.0.0.0";
    public static final String MF_CRISTIN_IDENTIFIER = "190.0.0.0";
    public static final String NIFU_OWNER_VALUE = "nifu@7463.0.0.0";
    public static final String NIFU_CRISTIN_IDENTIFIER = "7463.0.0.0";
    public static final String NIVA_OWNER_VALUE = "niva@7464.0.0.0";
    public static final String NIVA_CRISTIN_IDENTIFIER = "7464.0.0.0";
    public static final String PHS_OWNER_VALUE = "phs@233.0.0.0";
    public static final String PHS_CRISTIN_IDENTIFIER = "233.0.0.0";
    public static final String STAMI_OWNER_VALUE = "stami@7476.0.0.0";
    public static final String STAMI_CRISTIN_IDENTIFIER = "7476.0.0.0";
    public static final String STEINERHOYSKOLEN_OWNER_VALUE = "steinerhoyskolen@1525.0.0.0";
    public static final String STEINERHOYSKOLEN_CRISTIN_IDENTIFIER = "1525.0.0.0";
    public static final String VETINST_OWNER_VALUE = "vetinst@7497.0.0.0";
    public static final String VETINST_CRISTIN_IDENTIFIER = "7497.0.0.0";
    public static final String NASJONALMUSEET_OWNER_VALUE = "nasjonalmuseet@1305.0.0.0";
    public static final String NASJONALMUSEET_CRISTIN_IDENTIFIER = "1305.0.0.0";
    public static final String UNIT_OWNER_VALUE = "unit@195.0.0.0";
    public static final String UNIT_CRISTIN_IDENTIFIER = "195.0.0.0";
    public static final String STATPED_OWNER_VALUE = "statped@5831.0.0.0";
    public static final String STATPED_CRISTIN_IDENTIFIER = "5831.0.0.0";
    public static final String VEGVESEN_OWNER_VALUE = "vegvesen@6056.0.0.0";
    public static final String VEGVESEN_CRISTIN_IDENTIFIER = "6056.0.0.0";
    public static final String FFI_OWNER_VALUE = "ffi@7428.0.0.0";
    public static final String FFI_CRISTIN_IDENTIFIER = "7428.0.0.0";
    public static final String CRISTIN_IDENTIFIER = "CRISTIN_IDENTIFIER";
    public static final String OWNER_VALUE = "OWNER_VALUE";
    public static final String SANDBOX_URI = "https://api.sandbox.nva.aws.unit.no/cristin/organization/";
    public static final String DEVELOP_URI = "https://api.sandbox.nva.aws.unit.no/cristin/organization/";
    public static final String TEST_URI = "https://api.test.nva.aws.unit.no/cristin/organization/";
    public static final String PROD_URI = "https://api.nva.aws.unit.no/cristin/organization/";
    private static final String NB_CRISTIN_IDENTIFIER = "5931.0.0.0";
    private static final String NB_OWNER_VALUE = "nb@5931.0.0.0";
    private static final String NFORSK_CRISTIN_IDENTIFIER = "7446.0.0.0";
    private static final String NFORSK_OWNER_VALUE = "nforsk@7446.0.0.0";
    private static final String CICERO_CRISTIN_IDENTIFIER = "7475.0.0.0";
    private static final String CICERO_OWNER_VALUE = "cicero@7475.0.0.0";
    private static final String FNI_OWNER_VALUE = "fni@7430.0.0.0";
    private static final String FNI_CRISTIN_IDENTIFIER = "7430.0.0.0";
    private static final String NLA_OWNER_VALUE = "nla@54.0.0.0";
    private static final String NLA_CRISTIN_IDENTIFIER = "54.0.0.0";
    private static final String NINA_CRISTIN_IDENTIFIER = "7511.0.0.0";
    private static final String NINA_OWNER_VALUE = "nina@7511.0.0.0";
    private static final String NMBU_OWNER_VALUE = "nbmu@192.0.0.0";
    private static final String NMBU_CRISTIN_IDENTIFIER = "192.0.0.0";
    private static final String NMH_CRISTIN_IDENTIFIER = "178.0.0.0";
    private static final String NMH_OWNER_VALUE = "nmh@178.0.0.0";
    private static final String NOFIMA_CRISTIN_IDENTIFIER = "7543.0.0.0";
    private static final String NOFIMA_OWNER_VALUE = "nofima@7543.0.0.0";
    private static final String NORSERESEARCH_OWNER_VALUE = "norceresearch@2057.0.0.0";
    private static final String NORCERESEARCH_CRISTIN_IDENTIFIER = "2057.0.0.0";
    private static final String NORSKFOLKEMUSEUM_CRISTIN_IDENTIFIER = "1302.0.0.0";
    private static final String NORSKFOLKEMUSEUM_OWNER_VALUE = "norskfolkemuseum@1302.0.0.0";
    private static final String NPOLAR_CRISTIN_IDENTIFIER = "7466.0.0.0";
    private static final String NPOLAR_OWNER_VALUE = "npolar@7466.0.0.0";
    private static final String RURALIS_CRISTIN_IDENTIFIER = "7501.0.0.0";
    private static final String RURALIS_OWNER_VALUE = "ruralis@7501.0.0.0";
    private static final String SAMAS_CRISTIN_IDENTIFIER = "231.0.0.0";
    private static final String SAMAS_OWNER_VALUE = "samas@231.0.0.0";
    private static final String SAMFUNNSFORSKNING_CRISTIN_IDENTIFIER = "7437.0.0.0";
    private static final String SAMFUNNSFORSKNING_OWNER_VALUE = "samfunnsforskning@7437.0.0.0";
    private static final String SIHF_CRISTIN_IDENTIFIER = "1991.0.0.0";
    private static final String SIHF_OWNER_VALUE = "sihf@1991.0.0.0";
    private static final String SINTEF_CRISTIN_IDENTIFIER = "7401.0.0.0";
    private static final String SINTEF_OWNER_VALUE = "sintef@7401.0.0.0";
    private static final String SSB_CRISTIN_IDENTIFIER = "5932.0.0.0";
    private static final String SSB_OWNER_VALUE = "ssb@5932.0.0.0";
    private static final String UIS_CRISTIN_IDENTIFIER = "217.0.0.0";
    private static final String UIS_OWNER_VALUE = "uis@217.0.0.0";
    private static final String USN_CRISTIN_IDENTIFIER = "222.0.0.0";
    private static final String USN_OWNER_VALUE = "usn@222.0.0.0";
    private static final String NIKU_OWNER_VALUE = "niku@7530.0.0.0";
    private static final String NIKU_CRISTIN_IDENTIFIER = "7530.0.0.0";
    private static final String NILU_CRISTIN_IDENTIFIER = "7460.0.0.0";
    private static final String NILU_OWNER_VALUE = "nilu@7460.0.0.0";
    private static final String NGU_CRISTIN_IDENTIFIER = "7452.0.0.0";
    private static final String NGU_OWNER_VALUE = "ngu@7452.0.0.0";
    private static final String UIT_CRISTIN_IDENTIFIER = "186.0.0.0";
    private static final String UIT_OWNER_VALUE = "uit@186.0.0.0";
    private static final String UIO_CRISTIN_IDENTIFIER = "185.90.0.0";
    private static final String UIO_OWNER_VALUE = "uio@185.90.0.0";
    private static final String FHI_OWNER_VALUE = "fhi@7502.0.0.0";
    private static final String FHI_CRISTIN_IDENTIFIER = "7502.0.0.0";
    private static final String R_BUP_OWNER_VALUE = "r-bup@7539.0.0.0";
    private static final String R_BUP_CRISTIN_IDENTIFIER = "7539.0.0.0";
    private static final String IMR_OWNER_VALUE = "imr@7431.0.0.0";
    private static final String IMR_CRISTIN_IDENTIFIER = "7431.0.0.0";
    private static final String BANENOR_OWNER_VALUE = "banenor@21033.0.0.0";
    private static final String BANENOR_CRISTIN_IDENTIFIER = "21033.0.0.0";
    private static final String TOI_OWNER_VALUE = "toi@7482.0.0.0";
    private static final String TOI_CRISTIN_IDENTIFIER = "7482.0.0.0";
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private static final Map<String, Map<String, String>> RESOURCE_OWNER_MAP = new ConcurrentHashMap<>()
    {{
            put(NORGES_BANK, Map.ofEntries(entry(OWNER_VALUE, NORGES_BANK_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NORGES_BANK_CRISTIN_IDENTIFIER)));
            put(RA, Map.ofEntries(entry(OWNER_VALUE, RA_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, RA_CRISTIN_IDENTIFIER)));
            put(VID, Map.ofEntries(entry(OWNER_VALUE, VID_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, VID_CRISTIN_IDENTIFIER)));
            put(BI, Map.ofEntries(entry(OWNER_VALUE, BI_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, BI_CRISTIN_IDENTIFIER)));
            put(INN, Map.ofEntries(entry(OWNER_VALUE, INN_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, INN_CRISTIN_IDENTIFIER)));
            put(NUPI, Map.ofEntries(entry(OWNER_VALUE, NUPI_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NUPI_CRISTIN_IDENTIFIER)));
            put(NVE, Map.ofEntries(entry(OWNER_VALUE, NVE_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NVE_CRISTIN_IDENTIFIER)));
            put(KRUS, Map.ofEntries(entry(OWNER_VALUE, KRUS_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, KRUS_CRISTIN_IDENTIFIER)));
            put(KRISTIANIA, Map.ofEntries(entry(OWNER_VALUE, KRISTIANIA_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, KRISTIANIA_CRISTIN_IDENTIFIER)));
            put(FHS, Map.ofEntries(entry(OWNER_VALUE, FHS_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, FHS_CRISTIN_IDENTIFIER)));
            put(HIOF, Map.ofEntries(entry(OWNER_VALUE, HIOF_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, HIOF_CRISTIN_IDENTIFIER)));
            put(NGI, Map.ofEntries(entry(OWNER_VALUE, NGI_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NGI_CRISTIN_IDENTIFIER)));
            put(NIBIO, Map.ofEntries(entry(OWNER_VALUE, NIBIO_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NIBIO_CRISTIN_IDENTIFIER)));
            put(NIH, Map.ofEntries(entry(OWNER_VALUE, NIH_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NIH_CRISTIN_IDENTIFIER)));
            put(NTNU, Map.ofEntries(entry(OWNER_VALUE, NTNU_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NTNU_CRISTIN_IDENTIFIER)));
            put(NR, Map.ofEntries(entry(OWNER_VALUE, NR_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NR_CRISTIN_IDENTIFIER)));
            put(SAMFORSK, Map.ofEntries(entry(OWNER_VALUE, SAMFORSK_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, SAMFORSK_CRISTIN_IDENTIFIER)));
            put(CMI, Map.ofEntries(entry(OWNER_VALUE, CMI_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, CMI_CRISTIN_IDENTIFIER)));
            put(DMMH, Map.ofEntries(entry(OWNER_VALUE, DMMH_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, DMMH_CRISTIN_IDENTIFIER)));
            put(FAFO, Map.ofEntries(entry(OWNER_VALUE, FAFO_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, FAFO_CRISTIN_IDENTIFIER)));
            put(FDIR, Map.ofEntries(entry(OWNER_VALUE, FDIR_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, FDIR_CRISTIN_IDENTIFIER)));
            put(UIA, Map.ofEntries(entry(OWNER_VALUE, UIA_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, UIA_CRISTIN_IDENTIFIER)));
            put(NORD, Map.ofEntries(entry(OWNER_VALUE, NORD_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NORD_CRISTIN_IDENTIFIER)));
            put(ODA, Map.ofEntries(entry(OWNER_VALUE, ODA_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, ODA_CRISTIN_IDENTIFIER)));
            put(HIMOLDE, Map.ofEntries(entry(OWNER_VALUE, HIMOLDE_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, HIMOLDE_CRISTIN_IDENTIFIER)));
            put(HIVOLDA, Map.ofEntries(entry(OWNER_VALUE, HIVOLDA_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, HIVOLDA_CRISTIN_IDENTIFIER)));
            put(HVLOPEN, Map.ofEntries(entry(OWNER_VALUE, HVLOPEN_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, HVLOPEN_CRISTIN_IDENTIFIER)));
            put(NHH, Map.ofEntries(entry(OWNER_VALUE, NHH_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NHH_CRISTIN_IDENTIFIER)));
            put(BORA, Map.ofEntries(entry(OWNER_VALUE, BORA_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, BORA_CRISTIN_IDENTIFIER)));
            put(AHO, Map.ofEntries(entry(OWNER_VALUE, AHO_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, AHO_CRISTIN_IDENTIFIER)));
            put(IFE, Map.ofEntries(entry(OWNER_VALUE, IFE_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, IFE_CRISTIN_IDENTIFIER)));
            put(KHIO, Map.ofEntries(entry(OWNER_VALUE, KHIO_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, KHIO_CRISTIN_IDENTIFIER)));
            put(LDH, Map.ofEntries(entry(OWNER_VALUE, LDH_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, LDH_CRISTIN_IDENTIFIER)));
            put(MF, Map.ofEntries(entry(OWNER_VALUE, MF_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, MF_CRISTIN_IDENTIFIER)));
            put(NIFU, Map.ofEntries(entry(OWNER_VALUE, NIFU_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NIFU_CRISTIN_IDENTIFIER)));
            put(NIVA, Map.ofEntries(entry(OWNER_VALUE, NIVA_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NIVA_CRISTIN_IDENTIFIER)));
            put(PHS, Map.ofEntries(entry(OWNER_VALUE, PHS_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, PHS_CRISTIN_IDENTIFIER)));
            put(STAMI, Map.ofEntries(entry(OWNER_VALUE, STAMI_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, STAMI_CRISTIN_IDENTIFIER)));
            put(STEINERHOYSKOLEN, Map.ofEntries(entry(OWNER_VALUE, STEINERHOYSKOLEN_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, STEINERHOYSKOLEN_CRISTIN_IDENTIFIER)));
            put(VETINST, Map.ofEntries(entry(OWNER_VALUE, VETINST_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, VETINST_CRISTIN_IDENTIFIER)));
            put(NASJONALMUSEET, Map.ofEntries(entry(OWNER_VALUE, NASJONALMUSEET_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NASJONALMUSEET_CRISTIN_IDENTIFIER)));
            put(UNIT, Map.ofEntries(entry(OWNER_VALUE, UNIT_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, UNIT_CRISTIN_IDENTIFIER)));
            put(STATPED, Map.ofEntries(entry(OWNER_VALUE, STATPED_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, STATPED_CRISTIN_IDENTIFIER)));
            put(VEGVESEN, Map.ofEntries(entry(OWNER_VALUE, VEGVESEN_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, VEGVESEN_CRISTIN_IDENTIFIER)));
            put(FFI, Map.ofEntries(entry(OWNER_VALUE, FFI_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, FFI_CRISTIN_IDENTIFIER)));
            put(NB, Map.ofEntries(entry(OWNER_VALUE, NB_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NB_CRISTIN_IDENTIFIER)));
            put(NFORSK, Map.ofEntries(entry(OWNER_VALUE, NFORSK_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NFORSK_CRISTIN_IDENTIFIER)));
            put(CICERO, Map.ofEntries(entry(OWNER_VALUE, CICERO_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, CICERO_CRISTIN_IDENTIFIER)));
            put(FNI, Map.ofEntries(entry(OWNER_VALUE, FNI_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, FNI_CRISTIN_IDENTIFIER)));
            put(NLA, Map.ofEntries(entry(OWNER_VALUE, NLA_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NLA_CRISTIN_IDENTIFIER)));
            put(NINA, Map.ofEntries(entry(OWNER_VALUE, NINA_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NINA_CRISTIN_IDENTIFIER)));
            put(NMBU, Map.ofEntries(entry(OWNER_VALUE, NMBU_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NMBU_CRISTIN_IDENTIFIER)));
            put(NMH, Map.ofEntries(entry(OWNER_VALUE, NMH_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NMH_CRISTIN_IDENTIFIER)));
            put(NOFIMA, Map.ofEntries(entry(OWNER_VALUE, NOFIMA_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NOFIMA_CRISTIN_IDENTIFIER)));
            put(NORCERESEARCH, Map.ofEntries(entry(OWNER_VALUE, NORSERESEARCH_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NORCERESEARCH_CRISTIN_IDENTIFIER)));
            put(NORSKFOLKEMUSEUM, Map.ofEntries(entry(OWNER_VALUE, NORSKFOLKEMUSEUM_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NORSKFOLKEMUSEUM_CRISTIN_IDENTIFIER)));
            put(NPOLAR, Map.ofEntries(entry(OWNER_VALUE, NPOLAR_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NPOLAR_CRISTIN_IDENTIFIER)));
            put(RURALIS, Map.ofEntries(entry(OWNER_VALUE, RURALIS_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, RURALIS_CRISTIN_IDENTIFIER)));
            put(SAMAS, Map.ofEntries(entry(OWNER_VALUE, SAMAS_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, SAMAS_CRISTIN_IDENTIFIER)));
            put(SAMFUNNSFORSKNING, Map.ofEntries(entry(OWNER_VALUE, SAMFUNNSFORSKNING_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, SAMFUNNSFORSKNING_CRISTIN_IDENTIFIER)));
            put(SIHF, Map.ofEntries(entry(OWNER_VALUE, SIHF_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, SIHF_CRISTIN_IDENTIFIER)));
            put(SINTEF, Map.ofEntries(entry(OWNER_VALUE, SINTEF_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, SINTEF_CRISTIN_IDENTIFIER)));
            put(SSB, Map.ofEntries(entry(OWNER_VALUE, SSB_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, SSB_CRISTIN_IDENTIFIER)));
            put(UIS, Map.ofEntries(entry(OWNER_VALUE, UIS_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, UIS_CRISTIN_IDENTIFIER)));
            put(USN, Map.ofEntries(entry(OWNER_VALUE, USN_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, USN_CRISTIN_IDENTIFIER)));
            put(NIKU, Map.ofEntries(entry(OWNER_VALUE, NIKU_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NIKU_CRISTIN_IDENTIFIER)));
            put(NILU, Map.ofEntries(entry(OWNER_VALUE, NILU_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NILU_CRISTIN_IDENTIFIER)));
            put(NGU, Map.ofEntries(entry(OWNER_VALUE, NGU_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, NGU_CRISTIN_IDENTIFIER)));
            put(UIT, Map.ofEntries(entry(OWNER_VALUE, UIT_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, UIT_CRISTIN_IDENTIFIER)));
            put(UIO, Map.ofEntries(entry(OWNER_VALUE, UIO_OWNER_VALUE),
            entry(CRISTIN_IDENTIFIER, UIO_CRISTIN_IDENTIFIER)));
            put(FHI, Map.ofEntries(entry(OWNER_VALUE, FHI_OWNER_VALUE),
                                   entry(CRISTIN_IDENTIFIER, FHI_CRISTIN_IDENTIFIER)));
            put(R_BUP, Map.ofEntries(entry(OWNER_VALUE, R_BUP_OWNER_VALUE),
                               entry(CRISTIN_IDENTIFIER, R_BUP_CRISTIN_IDENTIFIER)));
        put(IMR, Map.ofEntries(entry(OWNER_VALUE, IMR_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, IMR_CRISTIN_IDENTIFIER)));
        put(BANENOR, Map.ofEntries(entry(OWNER_VALUE, BANENOR_OWNER_VALUE),
                               entry(CRISTIN_IDENTIFIER, BANENOR_CRISTIN_IDENTIFIER)));
        put(TOI, Map.ofEntries(entry(OWNER_VALUE, TOI_OWNER_VALUE),
                                   entry(CRISTIN_IDENTIFIER, TOI_CRISTIN_IDENTIFIER)));
    }};

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
