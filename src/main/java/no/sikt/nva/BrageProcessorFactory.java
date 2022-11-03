package no.sikt.nva;

import java.net.URI;
import java.util.Map;
import nva.commons.core.StringUtils;

public class BrageProcessorFactory {

    public static final String ZIP_EXTENSION = ".zip";
    private static final String INVALID_ZIPFILE_NAME_EXCEPTION_MESSAGE = "invalid zipfile name";

    private final Map<String, String> rescueTitleAndHandleMap;

    public BrageProcessorFactory(Map<String, String> rescueTitleAndHandleMap) {
        this.rescueTitleAndHandleMap = rescueTitleAndHandleMap;
    }

    public BrageProcessor createBrageProcessor(String zipfile, URI customerId, boolean enableOnlineValidation,
                                               boolean noHandleCheck) {
        var destinationDirectory = zipfile.replace(ZIP_EXTENSION, StringUtils.EMPTY_STRING);
        if (StringUtils.isEmpty(destinationDirectory)) {
            throw new RuntimeException(INVALID_ZIPFILE_NAME_EXCEPTION_MESSAGE);
        }
        return new BrageProcessor(zipfile, customerId, destinationDirectory, rescueTitleAndHandleMap,
                                  enableOnlineValidation, noHandleCheck);
    }
}
