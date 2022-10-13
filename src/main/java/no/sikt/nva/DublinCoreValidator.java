package no.sikt.nva;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;

public final class DublinCoreValidator {

    public static List<Problem> getDublinCoreErrors(DublinCore dublinCore) {
        var problems = new ArrayList<Problem>();
        if (hasCristinIdentifier(dublinCore)) {
            problems.add(Problem.CRISTIN_ID_PRESENT);
        }
        if(!hasInvalidType(dublinCore)) {
            problems.add(Problem.INVALID_TYPE);
        }
        return problems;
    }

    private static boolean hasCristinIdentifier(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isCristinDcValue);
    }

    private static boolean hasInvalidType(DublinCore dublinCore) {
        var types = dublinCore.getDcValues().stream()
                        .filter(DcValue::isType)
                        .map(DcValue::getValue)
                        .collect(Collectors.toList());
        try {
            TypeMapper.toNvaType(types);
            return true;
        } catch (DublinCoreException e) {
            return false;
        }
    }

    public enum Problem {
        CRISTIN_ID_PRESENT,
        INVALID_TYPE
    }
}
