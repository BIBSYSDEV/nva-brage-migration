package no.sikt.nva.validators;

import static no.sikt.nva.BrageProcessor.DEFAULT_LICENSE_FILE_NAME;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.scrapers.LicenseScraper;

public class BrageProcessorValidator {

    public static List<WarningDetails> getBrageProcessorWarnings(File entryDirectory, DublinCore dublinCore) {
        var warnings = new ArrayList<WarningDetails>();
        getCCLicenseWarnings(entryDirectory, dublinCore).ifPresent(warnings::add);
        return warnings;
    }

    private static Optional<WarningDetails> getCCLicenseWarnings(File entryDirectory, DublinCore dublinCore) {
        if (containsCCLicenseFile(entryDirectory)) {
            LicenseScraper licenseScraper = new LicenseScraper(DEFAULT_LICENSE_FILE_NAME);
            Optional<License> license = Optional.ofNullable(licenseScraper.extractOrCreateLicense(entryDirectory, dublinCore));
            if (LicenseScraper.isValidCCLicense(license.get())) {
                return Optional.empty();
            } else {
                return Optional.of(new WarningDetails(Warning.INVALID_CC_LICENSE,
                                                      license.orElse(new License()).toString()));
            }
        } else {
            return Optional.empty();
        }
    }

    private static boolean containsCCLicenseFile(File entry) {
        return Arrays.stream(Objects.requireNonNull(entry.listFiles()))
                   .anyMatch(file -> file.getName().equals(DEFAULT_LICENSE_FILE_NAME));
    }
}
