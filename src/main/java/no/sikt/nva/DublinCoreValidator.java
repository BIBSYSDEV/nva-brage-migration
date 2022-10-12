package no.sikt.nva;

import java.util.ArrayList;
import java.util.List;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;

public final class DublinCoreValidator {
    
    public enum Problem {
        CRISTIN_ID_PRESENT
    }

    public static List<Problem>  getDublinCoreErrors(DublinCore dublinCore) {
        var problems = new ArrayList<Problem>();
        if (hasCristinIdentifier(dublinCore)) {
            problems.add(Problem.CRISTIN_ID_PRESENT);
        }
        return problems;
    }

    private static boolean hasCristinIdentifier(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isCristinDcValue);
    }
}
