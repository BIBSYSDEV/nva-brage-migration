package no.sikt.nva;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import no.sikt.nva.exceptions.HandleException;
import no.sikt.nva.model.DcValue;
import no.sikt.nva.model.DublinCore;
import no.sikt.nva.model.Element;
import no.sikt.nva.model.Qualifier;
import nva.commons.core.paths.UriWrapper;

public class HandleScraper {

    //"dublin_core.xml" and "handle" both contains the handle.
    //Check dublin_core.xml first for handle and verify that the handle is in the correct format.
    // Something fails during handle scraping, retort to using the handle file.

    public static URI extractHandleFromDublinCore(final DublinCore dublinCore) {
        var handleDcValue =
            dublinCore.getDcValues().stream().filter(dcValue -> isIdentifierAndUri(dcValue)).findFirst();
        var handle = handleDcValue.orElseThrow(() -> new HandleException("No handle"
                                                                         + " "
                                                                         + "present"
                                                                         + " in "
                                                                         + "Doublin"
                                                                         + " Core"));
        var handleString = handle.getValue();

        return verifiedHandleURI(handleString);
    }

    public static URI extractHandleFromHandleFile(Path handlePath) {
        URI handle = null;
        return handle;
    }

    private static URI verifiedHandleURI(String handleString) {
        var handleUriWrapper = UriWrapper.fromUri(handleString);
        assert handleUriWrapper.getParent().isPresent();
        return handleUriWrapper.getUri();
    }

    private static boolean isIdentifierAndUri(DcValue dcValue) {
        if (Objects.isNull(dcValue.getElement()) || Objects.isNull(dcValue.getQualifier())) {
            return false;
        } else {
            return dcValue.getElement().equals(Element.IDENTIFIER) && dcValue.getQualifier().equals(Qualifier.URI);
        }
    }
}
