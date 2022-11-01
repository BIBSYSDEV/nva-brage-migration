package no.sikt.nva.channelregister;

import com.opencsv.bean.CsvBindByName;
import java.util.Optional;
import nva.commons.core.StringUtils;

public class ChannelRegisterJournal {

    @CsvBindByName(column = "Tidsskrift id")
    private String identifier;
    @CsvBindByName(column = "Print ISSN")
    private String printIssn;
    @CsvBindByName(column = "Online ISSN")
    private String onlineIssn;

    @CsvBindByName(column = "Original tittel")
    private String originalTitle;
    @CsvBindByName(column = "Internasjonal tittel")
    private String internationalTitle;

    public ChannelRegisterJournal() {

    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPrintIssn() {
        return printIssn;
    }

    public void setPrintIssn(String printIssn) {
        this.printIssn = printIssn;
    }

    public String getOnlineIssn() {
        return onlineIssn;
    }

    public void setOnlineIssn(String onlineIssn) {
        this.onlineIssn = onlineIssn;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getInternationalTitle() {
        return internationalTitle;
    }

    public void setInternationalTitle(String internationalTitle) {
        this.internationalTitle = internationalTitle;
    }

    public boolean hasIssn(String issn) {
        var onlineIssn = Optional.ofNullable(this.getOnlineIssn()).orElse(StringUtils.EMPTY_STRING);
        var printIssn = Optional.ofNullable(this.getPrintIssn()).orElse(StringUtils.EMPTY_STRING);

        return onlineIssn.equals(issn) || printIssn.equals(issn);
    }

    public boolean hasTitle(String title) {
        var originalTitle = Optional.ofNullable(this.getOriginalTitle()).orElse(StringUtils.EMPTY_STRING);
        var internationalTitle = Optional.ofNullable(this.getInternationalTitle()).orElse(StringUtils.EMPTY_STRING);

        return originalTitle.contains(title) || internationalTitle.contains(title);
    }
}
