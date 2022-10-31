package no.sikt.nva.scrapers;

import java.util.ArrayList;
import java.util.List;
import no.sikt.nva.model.ErrorDetails;
import no.sikt.nva.model.dublincore.DublinCore;

public class DublinCoreOnlineValidator {

    public static List<ErrorDetails> getDublinCoreErrors(DublinCore dublinCore) {

        var errors = new ArrayList<ErrorDetails>();
        DoiValidator.getDoiErrorDetailsOnline(dublinCore).ifPresent(errors::addAll);
        return errors;
    }
}
