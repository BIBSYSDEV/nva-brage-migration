package no.sikt.nva.scrapers.embargo;

public interface OnlineEmbargoChecker {

    boolean fileIsLockedOnline(String handle, String filename);

    void calculateCustomerAddress(String customer);
}
