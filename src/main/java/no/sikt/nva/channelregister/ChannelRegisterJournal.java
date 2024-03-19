package no.sikt.nva.channelregister;

import com.opencsv.bean.CsvBindByName;
import java.util.Locale;
import java.util.Optional;
import nva.commons.core.StringUtils;

public class ChannelRegisterJournal {

    @CsvBindByName(column = "PID")
    private String pid;
    @CsvBindByName(column = "Print ISSN")
    private String printIssn;
    @CsvBindByName(column = "Online ISSN")
    private String onlineIssn;

    @CsvBindByName(column = "Original tittel")
    private String originalTitle;

    public ChannelRegisterJournal() {

    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
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

    public boolean hasIssn(String issn) {
        var onlineIssn = Optional.ofNullable(this.getOnlineIssn()).orElse(StringUtils.EMPTY_STRING);
        var printIssn = Optional.ofNullable(this.getPrintIssn()).orElse(StringUtils.EMPTY_STRING);

        return onlineIssn.equals(issn) || printIssn.equals(issn);
    }

    public boolean hasTitle(String title) {
        var originalTitle = Optional.of(this.getOriginalTitle())
                                .map(value -> value.toLowerCase(Locale.getDefault()))
                                .orElse(StringUtils.EMPTY_STRING);

        return originalTitle.toLowerCase().equalsIgnoreCase(title.trim().toLowerCase(Locale.getDefault()));
    }
}
