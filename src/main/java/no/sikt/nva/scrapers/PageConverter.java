package no.sikt.nva.scrapers;

import java.util.regex.Pattern;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.record.Pages;
import no.sikt.nva.model.record.Range;
import nva.commons.core.StringUtils;

public class PageConverter {

    public static final String PAGE_POSTFIX = " s.";
    public static final String PAGE_PREFIX = "s. ";
    public static final String PAGE_INTERVAL_DELIMITER = "-";
    private static final String intervalRegex = "^[0-9]+-[0-9]+$";
    private static final Pattern intervalPattern = Pattern.compile(intervalRegex);
    private static final String numberRegex = "^[0-9]+$";
    private static final Pattern numberPattern = Pattern.compile(numberRegex);
    private static final String singleSpecificPage = "^s\\. [0-9]+$";
    private static final Pattern singleSpecificPagePattern = Pattern.compile(singleSpecificPage);
    private static final String multiplePagesWithPostfix = "^[0-9]+ s\\.$";
    private static final Pattern multiplePagesWithPostfixPattern = Pattern.compile(multiplePagesWithPostfix);
    private static final String SINGLE_PAGE = "1";
    private static final int START_PAGE = 0;
    private static final int END_PAGE = 1;

    public static Pages extractPages(DublinCore dublinCore) {
        var bragePages = dublinCore.getDcValues().stream()
                             .filter(DcValue::isPageNumber)
                             .findAny().map(DcValue::scrapeValueAndSetToScraped).orElse(null);
        if (StringUtils.isNotEmpty(bragePages)) {
            return createPages(bragePages);
        } else {
            return null;
        }
    }

    public static boolean isValidPageNumber(String bragePageNumber) {
        return pagesIsInterval(bragePageNumber)
               || pagesIsNumber(bragePageNumber)
               || isMultiplePagesWithPostfix(bragePageNumber)
               || pageIsSingleSpecifiedPage(bragePageNumber);
    }

    private static Pages createPages(String bragePages) {
        var pages = new Pages();
        //TODO: NVA-types affects what pages attributes should be set.
        pages.setBragePages(bragePages);
        if (pagesIsInterval(bragePages)) {
            pages.setRange(calculateRange(bragePages)); // This field depend on NVA-type
            pages.setPages(calculateNumberOfPagesFromRange(bragePages)); // This field depend on NVA-type
        } else if (pagesIsNumber(bragePages) || isMultiplePagesWithPostfix(bragePages)) {
            pages.setPages(bragePages.replace(PAGE_POSTFIX, StringUtils.EMPTY_STRING)); // This field depend on NVA-type
        } else if (pageIsSingleSpecifiedPage(bragePages)) {
            pages.setRange(calculateRangeFromSinglePage(bragePages)); // This field depend on NVA-type
            pages.setPages(SINGLE_PAGE); // This field depend on NVA-type
        }
        return pages;
    }

    private static String calculateNumberOfPagesFromRange(String bragePages) {
        var pages = bragePages.split(PAGE_INTERVAL_DELIMITER);
        var numberOfPages = Integer.parseInt(pages[END_PAGE]) - Integer.parseInt(pages[START_PAGE]);
        return Integer.toString(numberOfPages);
    }

    private static Range calculateRangeFromSinglePage(String bragePages) {
        var page = bragePages.replace(PAGE_PREFIX, StringUtils.EMPTY_STRING);
        return new Range(page, page);
    }

    private static boolean pageIsSingleSpecifiedPage(String bragePages) {
        return singleSpecificPagePattern.matcher(bragePages).matches();
    }

    private static boolean isMultiplePagesWithPostfix(String bragePages) {
        return multiplePagesWithPostfixPattern.matcher(bragePages).matches();
    }

    private static boolean pagesIsNumber(String bragePages) {
        return numberPattern.matcher(bragePages).matches();
    }

    private static Range calculateRange(String bragePages) {
        var pages = bragePages.split(PAGE_INTERVAL_DELIMITER);
        return new Range(pages[START_PAGE], pages[END_PAGE]);
    }

    private static boolean pagesIsInterval(String bragePages) {
        return intervalPattern.matcher(bragePages).matches();
    }
}
