package no.sikt.nva.scrapers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.model.ErrorDetails;
import no.sikt.nva.model.ErrorDetails.Error;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.doi.UnitHttpClient;

public class DoiValidator {

    public static final String HTTP_STRING = "http";
    public static final String HTTPS_STRING_FOR_DOI = "https://";

    public static Optional<ArrayList<ErrorDetails>> getDoiErrorDetailsOnline(DublinCore dublinCore) {
        var doiList = extractDoiList(dublinCore);
        return validateDoiListOnline(doiList);
    }

    public static Optional<ArrayList<ErrorDetails>> getDoiErrorDetailsOffline(DublinCore dublinCore) {
        var doiList = extractDoiList(dublinCore);
        return validateDoiListOffline(doiList);
    }

    private static Optional<ArrayList<ErrorDetails>> validateDoiListOnline(List<String> doiList) {
        var doiErrorList = new ArrayList<ErrorDetails>();
        for (String doi : doiList) {
            validateDoiOnline(doi).ifPresent(doiErrorList::add);
        }
        return getErrorDetails(doiErrorList);
    }

    private static Optional<ArrayList<ErrorDetails>> validateDoiListOffline(List<String> doiList) {
        var doiErrorList = new ArrayList<ErrorDetails>();
        for (String doi : doiList) {
            validateDoiOffline(doi).ifPresent(doiErrorList::add);
        }
        return getErrorDetails(doiErrorList);
    }

    @SuppressWarnings("PMD.LooseCoupling")
    private static Optional<ArrayList<ErrorDetails>> getErrorDetails(ArrayList<ErrorDetails> doiErrorList) {
        if (!doiErrorList.isEmpty()) {
            return Optional.of(doiErrorList);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<ErrorDetails> validateDoiOnline(String doi) {
        if (!isValidDoiOnline(doi)) {
            return Optional.of(new ErrorDetails(Error.INVALID_DOI_ONLINE_CHECK, List.of(doi)));
        }
        return Optional.empty();
    }

    private static Optional<ErrorDetails> validateDoiOffline(String doi) {
        if (!isValidDoiOffline(doi)) {
            return Optional.of(new ErrorDetails(Error.INVALID_DOI_OFFLINE_CHECK, List.of(doi)));
        }
        return Optional.empty();
    }

    private static boolean isValidDoiOnline(String doi) {
        var httpClient = new UnitHttpClient();
        var validator = new nva.commons.doi.DoiValidator(httpClient);
        try {
            validator.validateOnline(URI.create(doi));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String addHttpStringIfNotPresent(String doi) {
        if (doi.contains(HTTP_STRING)) {
            return doi;
        } else {
            return HTTPS_STRING_FOR_DOI + doi;
        }
    }

    private static List<String> extractDoiList(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isDoi)
                   .map(DcValue::getValue)
                   .map(DoiValidator::addHttpStringIfNotPresent)
                   .collect(Collectors.toList());
    }

    private static boolean isValidDoiOffline(String doi) {
        return nva.commons.doi.DoiValidator.validateOffline(doi);
    }
}
