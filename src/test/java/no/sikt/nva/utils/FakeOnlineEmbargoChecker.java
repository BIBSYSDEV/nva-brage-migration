package no.sikt.nva.utils;

import no.sikt.nva.scrapers.embargo.OnlineEmbargoChecker;

public class FakeOnlineEmbargoChecker implements OnlineEmbargoChecker {

    @Override
    public boolean fileIsLockedOnline(String handle, String filename) {
        return false;
    }

    @Override
    public void calculateCustomerAddress(String customer) {
        //Do nothing
    }
}
