package no.sikt.nva;

import java.util.List;
import java.util.Map;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.model.Embargo;
import nva.commons.core.StringUtils;

public class BrageProcessorFactory {

    public static final String ZIP_EXTENSION = ".zip";
    private static final String INVALID_ZIPFILE_NAME_EXCEPTION_MESSAGE = "invalid zipfile name";

    private final Map<String, String> rescueTitleAndHandleMap;
    private final List<Embargo> embargoes;
    private final List<Contributor> contributors;

    public BrageProcessorFactory(Map<String, String> rescueTitleAndHandleMap,
                                 List<Embargo> embargoes,
                                 List<Contributor> contributors) {
        this.rescueTitleAndHandleMap = rescueTitleAndHandleMap;
        this.embargoes = embargoes;
        this.contributors = contributors;
    }

    public BrageProcessor createBrageProcessor(String zipfile, String customer, boolean enableOnlineValidation,
                                               boolean shouldLookUpInChannelRegister,
                                               boolean noHandleCheck, String outputDirectory) {
        var destinationDirectory = outputDirectory + zipfile.replace(ZIP_EXTENSION, StringUtils.EMPTY_STRING);
        if (StringUtils.isEmpty(destinationDirectory)) {
            throw new RuntimeException(INVALID_ZIPFILE_NAME_EXCEPTION_MESSAGE);
        }
        return new BrageProcessor(zipfile, customer, destinationDirectory, rescueTitleAndHandleMap,
                                  enableOnlineValidation, shouldLookUpInChannelRegister, noHandleCheck, embargoes,
                                  contributors);
    }
}
