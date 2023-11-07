package no.sikt.nva.channelregister;

import com.opencsv.bean.CsvBindByName;
import java.util.Optional;
import nva.commons.core.StringUtils;

public class ChannelRegisterPublisher {

    @CsvBindByName(column = "PID")
    private String pid;
    @CsvBindByName(column = "Original tittel")
    private String originalTitle;

    public ChannelRegisterPublisher() {
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public boolean hasPublisher(String publisher) {
        var originalTitle = Optional.of(this.getOriginalTitle()).orElse(StringUtils.EMPTY_STRING);

        return originalTitle.equalsIgnoreCase(publisher.trim());
    }
}
