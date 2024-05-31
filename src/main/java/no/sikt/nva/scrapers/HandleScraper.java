package no.sikt.nva.scrapers;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import no.sikt.nva.brage.migration.aws.ColoredLogger;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.exceptions.HandleException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.core.StringUtils;
import nva.commons.core.paths.UriWrapper;

public class HandleScraper {

    public static final String ERROR_MESSAGE_NO_HANDLE_IN_DUBLIN_CORE = "No handle present in dublin_core.xml";
    public static final String COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE =
        "NO HANDLE";
    public static final String COULD_NOT_READ_HANDLE_FILE_EXCEPTION_MESSAGE = "Could not read handle file";
    public static final URI HANDLE_DOMAIN = UriWrapper.fromHost("https://hdl.handle.net").getUri();
    private static final ColoredLogger logger = ColoredLogger.create(HandleScraper.class);
    private static final String ERROR_MESSAGE_HANDLE_IN_DUBLIN_CORE_IS_MALFORMED = "Handle in dublin_core.xml is "
                                                                                   + "invalid: %s";

    public HandleScraper() {
    }

    /**
     * The first handle found is returned. Prioritized: handle-file and finally dublinCore.
     *
     * @param handlefile path to location that might contain the handle file.
     * @param dublinCore dublinCore potentially containing handle.
     * @param brageLocation bundle location
     * @return handle URI
     * @throws HandleException if it cannot find handle in rescueTitlesAndHandle map, handle nor dublinCore. Or if the
     *                         scraped handle-string does not conform the handle uri-scheme.
     */
    public URI scrapeHandle(final Path handlefile, final DublinCore dublinCore, BrageLocation brageLocation)
        throws HandleException {
        try {
            return extractHandleFromBundle(handlefile, dublinCore, brageLocation);
        } catch (Exception e) {
            logger.error(
                "Could not extract handle for brage location " + brageLocation.getBrageBundlePath().toString());
            throw new HandleException(COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE, e);
        }
    }

    private URI extractHandleFromHandlePath(final Path handlePath, BrageLocation brageLocation) throws HandleException {
        String handleSubPath;
        try {
            handleSubPath = Files.readString(handlePath);
        } catch (IOException e) {
            logger.error(COULD_NOT_READ_HANDLE_FILE_EXCEPTION_MESSAGE + brageLocation.getBrageBundlePath());
            throw new HandleException(COULD_NOT_READ_HANDLE_FILE_EXCEPTION_MESSAGE, e);
        }
        var handle = UriWrapper.fromUri(HANDLE_DOMAIN).addChild(handleSubPath.trim()).getUri();
        verifyHandle(handle);
        return handle;
    }

    private URI extractHandleFromDublinCore(final DublinCore dublinCore) throws HandleException {
        var dcValueHandle = extractDcValueContainingHandleFromDublinCore(dublinCore);
        var handleString = dcValueHandle.scrapeValueAndSetToScraped();
        return verifiedHandleURI(handleString);
    }

    private DcValue extractDcValueContainingHandleFromDublinCore(final DublinCore dublinCore)
        throws HandleException {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isHandle)
                   .findFirst()
                   .orElseThrow(() -> new HandleException(ERROR_MESSAGE_NO_HANDLE_IN_DUBLIN_CORE));
    }

    private URI verifiedHandleURI(final String handleString) throws HandleException {
        var handle = UriWrapper.fromUri(handleString).getUri();
        verifyHandle(handle);
        return handle;
    }

    private void verifyHandle(final URI handle) throws HandleException {
        if (!handle.getAuthority().equals(HANDLE_DOMAIN.getAuthority())
            || StringUtils.isBlank(handle.getPath())
            || missingPrefixOrPostFix(handle.getPath())) {

            throw new HandleException(String.format(ERROR_MESSAGE_HANDLE_IN_DUBLIN_CORE_IS_MALFORMED,
                                                    handle));
        }
    }

    private boolean missingPrefixOrPostFix(String path) {
        var pathElements = (int) Arrays.stream(path.split("/"))
                                     .filter(StringUtils::isNotBlank)
                                     .count();
        return pathElements < 2;
    }

    private URI extractHandleFromBundle(final Path handleFile, final DublinCore dublinCore, BrageLocation brageLocation) throws HandleException {
        try {
            return extractHandleFromHandlePath(handleFile, brageLocation);
        } catch (HandleException handleException) {
            return extractHandleFromDublinCore(dublinCore);
        }
    }
}
