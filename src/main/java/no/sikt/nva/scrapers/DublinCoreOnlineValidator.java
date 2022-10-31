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
import nva.commons.doi.DoiValidator;
import nva.commons.doi.UnitHttpClient;
import org.jetbrains.annotations.NotNull;

public class DublinCoreOnlineValidator {

    public static final String HTTP_STRING = "http";
    public static final String HTTPS_STRING_FOR_DOI = "https://";

    public static List<ErrorDetails> getDublinCoreErrors(DublinCore dublinCore) {

        var errors = new ArrayList<ErrorDetails>();
        getDoiErrorDetails(dublinCore).ifPresent(errors::add);
        return errors;
    }

    private static Optional<ErrorDetails> getDoiErrorDetails(DublinCore dublinCore) {
        var doiList =
            dublinCore.getDcValues()
                .stream()
                .filter(DcValue::isDoi)
                .map(DcValue::getValue)
                .map(DublinCoreOnlineValidator::addHttpStringIfNotPresent)
                .collect(Collectors.toList());

        return validateDoiList(doiList);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @NotNull
    private static Optional<ErrorDetails> validateDoiList(List<String> doiList) {
        if (doiList.isEmpty()) {
            return Optional.empty();
        } else {
            for (String doi : doiList) {
                if (!isValidDoiOnline(doi)) {
                    return Optional.of(new ErrorDetails(Error.INVALID_DOI_ONLINE_CHECK, doiList));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isValidDoiOnline(String doi) {
        var httpClient = new UnitHttpClient();
        var validator = new DoiValidator(httpClient);
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
}
