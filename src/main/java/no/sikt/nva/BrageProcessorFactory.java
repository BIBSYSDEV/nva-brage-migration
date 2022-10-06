package no.sikt.nva;

import nva.commons.core.StringUtils;

public final class BrageProcessorFactory {

    public static final String ZIP_EXTENSION = ".zip";
    private static final String INVALID_ZIPFILE_NAME_EXCEPTION_MESSAGE = "invalid zipfile name";

    private BrageProcessorFactory() {

    }

    public static BrageProcessor createBrageProcessor(String zipfile, String customerId) {
        var destinationDirectory = zipfile.replace(ZIP_EXTENSION, StringUtils.EMPTY_STRING);
        if (StringUtils.isEmpty(destinationDirectory)) {
            throw new RuntimeException(INVALID_ZIPFILE_NAME_EXCEPTION_MESSAGE);
        }
        return new BrageProcessor(zipfile, customerId, destinationDirectory);
    }
}
