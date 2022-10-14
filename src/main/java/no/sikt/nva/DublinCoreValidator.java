package no.sikt.nva;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;

public final class DublinCoreValidator {

    public static final String VERSION_STRING_NVE = "publishedVersion";


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

    public static Boolean hasPresentValidVersion(DublinCore dublinCore) {
        var versionList = dublinCore.getDcValues().stream()
                          .filter(DcValue::isVersion)
                          .collect(Collectors.toList());

        if(versionList.size() == 1) {
            if (VERSION_STRING_NVE.equals(version.get().getValue())) {
                return true;
            }
        }
    }

    private static Boolean isValidVersion(Optional<DcValue> version) {
        return version.map(dcValue -> isPresentValidVersion(version)).orElse(null);
    }

    private static Boolean isPresentValidVersion(Optional<DcValue> version) {
         else {
            logger.warn("Invalid version");
            return null;
        }
    }
}
