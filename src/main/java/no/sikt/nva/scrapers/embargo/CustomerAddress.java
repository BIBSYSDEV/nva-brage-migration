package no.sikt.nva.scrapers.embargo;

import com.opencsv.bean.CsvBindByName;
import nva.commons.core.JacocoGenerated;

public class CustomerAddress {

    @CsvBindByName(column = "Customer")
    private String customer;

    @CsvBindByName(column = "address")
    private String adress;

    @JacocoGenerated
    public CustomerAddress() {
    }

    @JacocoGenerated
    public String getCustomer() {
        return customer;
    }

    @JacocoGenerated
    public void setCustomer(String customer) {
        this.customer = customer;
    }

    @JacocoGenerated
    public String getAdress() {
        return adress;
    }

    @JacocoGenerated
    public void setAdress(String adress) {
        this.adress = adress;
    }
}
