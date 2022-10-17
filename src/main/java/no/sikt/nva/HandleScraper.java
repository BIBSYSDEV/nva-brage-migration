package no.sikt.nva;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import no.sikt.nva.exceptions.HandleException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.core.StringUtils;
import nva.commons.core.paths.UriWrapper;

public class HandleScraper {

    public static final String ERROR_MESSAGE_NO_HANDLE_IN_DUBLIN_CORE = "No handle present in dublin_core.xml";
    public static final String COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV =
        "Could not find handle in handle-file nor dublin_core or in supplied csv";
    private static final String ERROR_MESSAGE_HANDLE_IN_DUBLIN_CORE_IS_MALFORMED = "Handle in dublin_core.xml is "
                                                                                   + "invalid: %s";
    private static final String COULD_NOT_READ_HANDLE_FILE_EXCEPTION_MESSAGE = "Could not read handle file";
    public static final URI HANDLE_DOMAIN = UriWrapper.fromHost("https://hdl.handle.net").getUri();
    private final Map<String, String> titlesAndHandles;

    public HandleScraper(Map<String, String> titlesAndHandles) {
        this.titlesAndHandles = titlesAndHandles;
    }

    /**
     * The first handle found is returned. Prioritized: titlesAndHandles, handle-file and finally dublinCore.
     *
     * @param handlefile path to location that might contain the handle file.
     * @param dublinCore dublinCore potentially containing handle.
     * @return handle URI
     * @throws HandleException if it cannot find handle in rescueTitlesAndHandle map, handle nor dublinCore. Or if the
     *                         scraped handle-string does not conform the handle uri-scheme.
     */
    public URI scrapeHandle(final Path handlefile, final DublinCore dublinCore) throws HandleException {
        try {
            var optionalHandle = getHandleFromInTitlesAndHandlesMap(dublinCore);
            if (optionalHandle.isPresent()) {
                return optionalHandle.get();
            } else {
                return extractHandleFromBundle(handlefile, dublinCore);
            }
        } catch (Exception e) {
            throw new HandleException(COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV, e);
        }
    }

    private URI extractHandleFromHandlePath(final Path handlePath) throws HandleException {
        String handleSubPath;
        try {
            handleSubPath = Files.readString(handlePath);
        } catch (IOException e) {
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
                   .filter(DcValue::isUriIdentifier)
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
            || StringUtils.isBlank(handle.getPath())) {
            throw new HandleException(String.format(ERROR_MESSAGE_HANDLE_IN_DUBLIN_CORE_IS_MALFORMED,
                                                    handle));
        }
    }

    private Optional<URI> getHandleFromInTitlesAndHandlesMap(final DublinCore dublinCore) throws HandleException {
        var title = DublinCoreParser.extractTitle(dublinCore);
        if (StringUtils.isNotEmpty(title)) {
            var handle = titlesAndHandles.get(title);
            if (StringUtils.isNotEmpty(handle)) {
                return Optional.of(verifiedHandleURI(handle));
            }
        }
        return Optional.empty();
    }

    private URI extractHandleFromBundle(final Path handleFile, final DublinCore dublinCore) throws HandleException {
        try {
            return extractHandleFromHandlePath(handleFile);
        } catch (HandleException handleException) {
            return extractHandleFromDublinCore(dublinCore);
        }
    }
}
