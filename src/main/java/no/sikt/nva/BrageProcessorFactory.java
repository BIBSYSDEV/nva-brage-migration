package no.sikt.nva;

import static java.util.Objects.isNull;
import java.nio.file.Path;
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
    private final Map<String, Contributor> contributors;

    public BrageProcessorFactory(Map<String, String> rescueTitleAndHandleMap,
                                 List<Embargo> embargoes,
                                 Map<String, Contributor> contributors) {
        this.rescueTitleAndHandleMap = rescueTitleAndHandleMap;
        this.embargoes = embargoes;
        this.contributors = contributors;
    }

    public BrageProcessor createBrageProcessor(String zipfile, String customer, boolean enableOnlineValidation,
                                               boolean shouldLookUpInChannelRegister,
                                               boolean noHandleCheck, String awsEnvironment, String outputDirectory) {
        var destinationDirectory = generateDestinationDirectory(outputDirectory, zipfile);
        if (StringUtils.isEmpty(destinationDirectory)) {
            throw new RuntimeException(INVALID_ZIPFILE_NAME_EXCEPTION_MESSAGE);
        }
        return new BrageProcessor(zipfile, customer, destinationDirectory, rescueTitleAndHandleMap,
                                  enableOnlineValidation, shouldLookUpInChannelRegister, noHandleCheck,
                                  awsEnvironment, embargoes,
                                  contributors);
    }

    private static int getLength(String zipfile) {
        return zipfile.split("/").length;
    }

    private String generateDestinationDirectory(String outputDirectory, String zipfile) {
        var zipfileName = zipfile.split("/")[getLength(zipfile) - 1]
                              .replace(ZIP_EXTENSION, StringUtils.EMPTY_STRING);
        if (isNull(outputDirectory)) {
            return Path.of(zipfileName).toString();
        }
        return Path.of(outputDirectory, zipfileName).toString();
    }
}
