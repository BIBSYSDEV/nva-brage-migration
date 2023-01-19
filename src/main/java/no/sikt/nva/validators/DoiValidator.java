package no.sikt.nva.validators;

import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DOI_OFFLINE_CHECK;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DOI_ONLINE_CHECK;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.core.StringUtils;
import nva.commons.doi.UnitHttpClient;

public class DoiValidator {

    public static final String HTTP_STRING = "http";
    public static final String HTTPS_STRING = "https://";
    public static final String DOI_DOMAIN_NAME = "doi.org/";
    public static final String COLON = ":";

    public static Optional<ArrayList<ErrorDetails>> getDoiErrorDetailsOnline(DublinCore dublinCore) {
        var doiList = extractDoiList(dublinCore);
        var filteredDoiList = doiList.stream().filter(doi -> !doi.isEmpty()).collect(Collectors.toList());
        if (filteredDoiList.isEmpty()) {
            return Optional.empty();
        } else {
            var updatedUriDoiList = filteredDoiList.stream()
                                        .map(DoiValidator::updateDoiStructureIfNeeded)
                                        .collect(Collectors.toList());
            return validateDoiListOnline(updatedUriDoiList);
        }
    }

    public static Optional<ArrayList<ErrorDetails>> getDoiErrorDetailsOffline(DublinCore dublinCore) {
        var doiList = extractDoiList(dublinCore);
        var filteredDoiList = doiList.stream().filter(doi -> !doi.isEmpty()).collect(Collectors.toList());
        if (filteredDoiList.isEmpty()) {
            return Optional.empty();
        } else {
            var updatedUriDoiList = filteredDoiList.stream()
                                        .map(DoiValidator::updateDoiStructureIfNeeded)
                                        .collect(Collectors.toList());
            return validateDoiListOffline(updatedUriDoiList);
        }
    }

    public static String updateDoiStructureIfNeeded(String inputDoi) {
        var doi = removeEmptySpaces(inputDoi);
        if (doi.contains(HTTP_STRING)) {
            return doi;
        }
        if (doi.contains(DOI_DOMAIN_NAME)) {
            return HTTPS_STRING + doi;
        }
        if (doi.contains(COLON)) {
            return handleDoiWithColon(doi);
        } else {
            return HTTPS_STRING + DOI_DOMAIN_NAME + doi;
        }
    }

    private static String handleDoiWithColon(String doi) {
        var doiPath = doi.split(COLON)[1];
        if (doiPath.contains(HTTPS_STRING)) {
            return doiPath;
        }
        if (doiPath.contains(DOI_DOMAIN_NAME)) {
            return HTTPS_STRING + doiPath;
        } else {
            return HTTPS_STRING + DOI_DOMAIN_NAME + doiPath;
        }
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
            return Optional.of(new ErrorDetails(INVALID_DOI_ONLINE_CHECK, List.of(doi)));
        }
        return Optional.empty();
    }

    private static Optional<ErrorDetails> validateDoiOffline(String doi) {
        if (!isValidDoiOffline(doi)) {
            return Optional.of(new ErrorDetails(INVALID_DOI_OFFLINE_CHECK, List.of(doi)));
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

    private static List<String> extractDoiList(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isDoi)
                   .map(DcValue::getValue)
                   .collect(Collectors.toList());
    }

    private static boolean isValidDoiOffline(String doi) {
        return nva.commons.doi.DoiValidator.validateOffline(doi);
    }

    private static String removeEmptySpaces(String doi) {
        return doi.replaceAll("\\s", StringUtils.EMPTY_STRING);
    }
}