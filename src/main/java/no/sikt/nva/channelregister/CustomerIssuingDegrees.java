package no.sikt.nva.channelregister;

import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public class CustomerIssuingDegrees {

    private String brage;
    private String cristinId;
    private String cristinName;
    private String channelRegistryPidTest;
    private String channelRegistryPidProd;

    @JacocoGenerated
    public CustomerIssuingDegrees() {

    }

    @JacocoGenerated
    public String getBrage() {
        return brage;
    }

    @JacocoGenerated
    public void setBrage(String brage) {
        this.brage = brage;
    }

    @JacocoGenerated
    public String getCristinId() {
        return cristinId;
    }

    @JacocoGenerated
    public void setCristinId(String cristinId) {
        this.cristinId = cristinId;
    }

    @JacocoGenerated
    public String getCristinName() {
        return cristinName;
    }

    @JacocoGenerated
    public void setCristinName(String cristinName) {
        this.cristinName = cristinName;
    }

    @JacocoGenerated
    public String getChannelRegistryPidTest() {
        return channelRegistryPidTest;
    }

    @JacocoGenerated
    public void setChannelRegistryPidTest(String channelRegistryPidTest) {
        this.channelRegistryPidTest = channelRegistryPidTest;
    }

    @JacocoGenerated
    public String getChannelRegistryPidProd() {
        return channelRegistryPidProd;
    }

    @JacocoGenerated
    public void setChannelRegistryPidProd(String channelRegistryPidProd) {
        this.channelRegistryPidProd = channelRegistryPidProd;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomerIssuingDegrees)) {
            return false;
        }
        CustomerIssuingDegrees that = (CustomerIssuingDegrees) o;
        return Objects.equals(brage, that.brage)
               && Objects.equals(cristinId, that.cristinId)
               && Objects.equals(cristinName, that.cristinName)
               && Objects.equals(channelRegistryPidTest, that.channelRegistryPidTest)
               && Objects.equals(channelRegistryPidProd, that.channelRegistryPidProd);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(brage, cristinId, cristinName, channelRegistryPidTest, channelRegistryPidProd);
    }
}
