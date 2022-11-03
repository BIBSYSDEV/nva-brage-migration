package no.sikt.nva.channelregister;

import java.util.ArrayList;
import java.util.List;

public class CsvTransfer {

    private List<ChannelRegisterJournal> csvList;

    public CsvTransfer() {
    }

    public List<ChannelRegisterJournal> getCsvList() {
        if (csvList != null) {
            return csvList;
        }
        return new ArrayList<ChannelRegisterJournal>();
    }

    public void setCsvList(List<ChannelRegisterJournal> csvList) {
        this.csvList = csvList;
    }
}
