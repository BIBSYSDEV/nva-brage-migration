package no.sikt.nva;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import no.sikt.nva.exceptions.HandleException;
import no.sikt.nva.model.DcValue;
import no.sikt.nva.model.DublinCore;
import no.sikt.nva.model.Element;
import no.sikt.nva.model.Qualifier;
import nva.commons.core.StringUtils;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;

public class HandleScraper {

    public static final String ERROR_MESSAGE_NO_HANDLE_IN_DUBLIN_CORE = "No handle present in dublin_core.xml";
    public static final String ERROR_MESSAGE_HANDLE_IN_DUBLIN_CORE_IS_MALFORMED = "Handle in dublin_core.xml is "
                                                                                  + "invalid: %s";

    private static final URI HANDLE_DOMAIN = UriWrapper.fromHost("https://hdl.handle.net").getUri();

    public static URI extractHandleFromDublinCore(final DublinCore dublinCore) {
        var dcValueHandle = extractDvValueContainingHandleFromDublinCore(dublinCore);
        var handleString = dcValueHandle.getValue();

        return verifiedHandleURI(handleString);
    }

    public static URI extractHandleFromHandlePath(Path handlePath) {
        String handleSubPath = IoUtils.stringFromResources(handlePath).trim();
        var handle = UriWrapper.fromUri(HANDLE_DOMAIN).addChild(handleSubPath).getUri();
        verifyHandle(handle);
        return handle;
    }

    private static DcValue extractDvValueContainingHandleFromDublinCore(DublinCore dublinCore) {
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
