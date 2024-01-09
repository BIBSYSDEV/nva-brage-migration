package no.sikt.nva.channelregister;

import com.opencsv.bean.CsvBindByName;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public class ChannelRegisterAlias {

    @CsvBindByName(column = "alias")
    private String alias;

    @CsvBindByName(column = "Original tittel")
    private String originalTitle;

    @JacocoGenerated
    public ChannelRegisterAlias() {

    }

    public String getAlias() {
        return alias;
    }

    @JacocoGenerated
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @JacocoGenerated
    public String getOriginalTitle() {
        return originalTitle;
    }

    @JacocoGenerated
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public boolean hasAlias(String title) {
        return getAlias().equalsIgnoreCase(title.trim());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(alias, originalTitle);
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelRegisterAlias that = (ChannelRegisterAlias) o;
        return Objects.equals(alias, that.alias)
               && Objects.equals(originalTitle, that.originalTitle);
    }
}
