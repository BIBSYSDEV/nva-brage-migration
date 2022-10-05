package no.sikt.nva;

import nva.commons.core.StringUtils;

public final class BrageProcessorFactory {

    private static final String FILE_TYPE_EXTENSION_DELIMITER = ".";
    private static final int FILENAME_PART = 0;
    private static final String INVALID_ZIPFILE_NAME_EXCEPTION_MESSAGE = "invalid zipfile name";

    private BrageProcessorFactory() {

    }

    public static BrageProcessor createBrageProcessor(String zipfile, String customerId) {
        var destinationDirectory = zipfile.split(FILE_TYPE_EXTENSION_DELIMITER)[FILENAME_PART];
        if (StringUtils.isEmpty(destinationDirectory)) {
            throw new RuntimeException(INVALID_ZIPFILE_NAME_EXCEPTION_MESSAGE);
        }
        return new BrageProcessor(zipfile, customerId, destinationDirectory);
    }
}
