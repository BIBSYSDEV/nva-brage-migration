package no.sikt.nva.channelregister;

import com.opencsv.bean.CsvBindByName;

public class ChannelRegisterWildcard {

    @CsvBindByName(column = "wildcard")
    private String wildcard;
    @CsvBindByName(column = "name")
    private String name;

    public ChannelRegisterWildcard(String value, String identifier) {
        this.wildcard = value;
        this.name = identifier;
    }

    public ChannelRegisterWildcard() {
    }

    public String getWildcard() {
        return wildcard;
    }

    public String getName() {
        return name;
    }
}
