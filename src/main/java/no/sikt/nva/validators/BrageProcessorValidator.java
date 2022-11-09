package no.sikt.nva.validators;

import static no.sikt.nva.BrageProcessor.DEFAULT_LICENSE_FILE_NAME;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.WarningDetails.Warning;
import no.sikt.nva.scrapers.LicenseScraper;

public class BrageProcessorValidator {

    public static List<WarningDetails> getBrageProcessorWarnings(File entryDirectory) {
        var warnings = new ArrayList<WarningDetails>();
        getCCLicenseWarnings(entryDirectory).ifPresent(warnings::add);
        return warnings;
    }

    private static Optional<WarningDetails> getCCLicenseWarnings(File entryDirectory) {
        LicenseScraper licenseScraper = new LicenseScraper(DEFAULT_LICENSE_FILE_NAME);
        var license = licenseScraper.extractOrCreateLicense(entryDirectory);
        if (licenseScraper.isValidCCLicense(license)) {
            return Optional.empty();
        } else {
            return Optional.of(new WarningDetails(Warning.INVALID_CC_LICENSE));
        }
    }
}
