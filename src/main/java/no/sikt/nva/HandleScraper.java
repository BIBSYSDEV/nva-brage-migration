package no.sikt.nva;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import no.sikt.nva.exceptions.HandleException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import nva.commons.core.StringUtils;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandleScraper {

    public static final String ERROR_MESSAGE_NO_HANDLE_IN_DUBLIN_CORE = "No handle present in dublin_core.xml";
    public static final String ERROR_MESSAGE_HANDLE_IN_DUBLIN_CORE_IS_MALFORMED = "Handle in dublin_core.xml is "
                                                                                  + "invalid: %s";
    public static final String COULD_NOT_READ_HANDLE_FILE_EXCEPTION_MESSAGE = "Could not read handle file";
    private static final URI HANDLE_DOMAIN = UriWrapper.fromHost("https://hdl.handle.net").getUri();
    private static final Logger logger = LoggerFactory.getLogger(HandleScraper.class);

    /**
     * Extracts handle from either "handle" file or dublin_core.xml. Will prioritize "handle" file.
     *
     * @param handleFile     tries to read handle from handleFile first.
     * @param dublinCoreFile containing the dublin_core.xml.
     * @return handle URI
     * @throws HandleException if neither handlePath nor dublin_core.xml yields hansle.
     */
    public static URI extractHandleFromBundle(Path handleFile, File dublinCoreFile) {
        try {
            return extractHandleFromHandlePath(handleFile);
        } catch (HandleException handleException) {
            logger.warn(handleException.getMessage());
            var dublinCore = DublinCoreParser.unmarshallDublinCore(dublinCoreFile, StringUtils.EMPTY_STRING);
            return extractHandleFromDublinCore(dublinCore);
        }
    }

    /**
     * Preferred method of extracting handle.
     *
     * @param handlePath path to the handle file
     * @return handle URI
     * @throws HandleException if handle file is empty or contains domain in addition to handle sub-path
     */
    public static URI extractHandleFromHandlePath(Path handlePath) {
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

    /**
     * Extracts handle from a pre-marshalled dublin_core.xml. This method should be used if
     * extractHandleFromHandlePath(handlePath) fails.
     *
     * @param dublinCore xml unmarshalled
     * @return Handle URI
     * @throws HandleException if handle is not a handle URI
     */
    public static URI extractHandleFromDublinCore(final DublinCore dublinCore) {
        var dcValueHandle = extractDcValueContainingHandleFromDublinCore(dublinCore);
        var handleString = dcValueHandle.getValue();
        return verifiedHandleURI(handleString);
    }

    private static DcValue extractDcValueContainingHandleFromDublinCore(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(HandleScraper::isIdentifierAndUri)
                   .findFirst()
                   .orElseThrow(() -> new HandleException(ERROR_MESSAGE_NO_HANDLE_IN_DUBLIN_CORE));
    }

    private static URI verifiedHandleURI(String handleString) {
        var handle = UriWrapper.fromUri(handleString).getUri();
        verifyHandle(handle);
        return handle;
    }

    private static void verifyHandle(final URI handle) {
        if (!handle.getAuthority().equals(HANDLE_DOMAIN.getAuthority())
            || StringUtils.isBlank(handle.getPath())) {
            throw new HandleException(String.format(ERROR_MESSAGE_HANDLE_IN_DUBLIN_CORE_IS_MALFORMED,
                                                    handle));
        }
    }

    private static boolean isIdentifierAndUri(DcValue dcValue) {
        if (Objects.isNull(dcValue.getElement()) || Objects.isNull(dcValue.getQualifier())) {
            return false;
        } else {
            return dcValue.getElement().equals(Element.IDENTIFIER) && dcValue.getQualifier().equals(Qualifier.URI);
        }
    }
}
