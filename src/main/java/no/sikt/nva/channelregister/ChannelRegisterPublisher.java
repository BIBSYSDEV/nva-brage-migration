package no.sikt.nva.channelregister;

import com.opencsv.bean.CsvBindByName;
import java.util.Optional;
import nva.commons.core.StringUtils;

public class ChannelRegisterPublisher {

    @CsvBindByName(column = "Forlag id")
    private String identifier;
    @CsvBindByName(column = "Original tittel")
    private String originalTitle;
    @CsvBindByName(column = "Internasjonal tittel")
    private String internationalTitle;
    @CsvBindByName(column = "ISBN-prefix")
    private String isbnPrefix;

    public ChannelRegisterPublisher() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public String getIsbnPrefix() {
        return isbnPrefix;
    }

    public void setIsbnPrefix(String isbnPrefix) {
        this.isbnPrefix = isbnPrefix;
    }

    public boolean hasIsbn(String isbn) {
        var isbnPrefix = Optional.ofNullable(this.getIsbnPrefix()).orElse(StringUtils.EMPTY_STRING);
        return isbnPrefix.equals(isbn);
    }

    public boolean hasPublisher(String publisher) {
        var originalTitle = Optional.of(this.getOriginalTitle()).orElse(StringUtils.EMPTY_STRING);
        var internationalTitle = Optional.of(this.getInternationalTitle()).orElse(StringUtils.EMPTY_STRING);

        return originalTitle.equalsIgnoreCase(publisher) || internationalTitle.equalsIgnoreCase(publisher);
    }
}
