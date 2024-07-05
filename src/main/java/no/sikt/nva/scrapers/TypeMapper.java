package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_UNMAPPABLE_TYPES;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.NvaType;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TypeMapper {

    private static final Logger logger = LoggerFactory.getLogger(TypeMapper.class);
    private static final Map<Set<BrageType>, NvaType> TYPE_MAP = Map.ofEntries(
        entry(Set.of(BrageType.BOOK, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_MONOGRAPH),
        entry(Set.of(BrageType.ACADEMIC_MONOGRAPH), NvaType.SCIENTIFIC_MONOGRAPH),
        entry(Set.of(BrageType.CHAPTER, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_CHAPTER),
        entry(Set.of(BrageType.ACADEMIC_CHAPTER), NvaType.SCIENTIFIC_CHAPTER),
        entry(Set.of(BrageType.ACADEMIC_CHAPTER, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_CHAPTER),
        entry(Set.of(BrageType.JOURNAL_ARTICLE, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_ARTICLE),
        entry(Set.of(BrageType.JOURNAL_ISSUE, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_ARTICLE),
        entry(Set.of(BrageType.ACADEMIC_ARTICLE), NvaType.SCIENTIFIC_ARTICLE),
        entry(Set.of(BrageType.ACADEMIC_ARTICLE, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_ARTICLE),
        entry(Set.of(BrageType.BOOK), NvaType.BOOK),
        entry(Set.of(BrageType.NON_FICTION_MONOGRAPH), NvaType.BOOK),
        entry(Set.of(BrageType.ANTHOLOGY), NvaType.ANTHOLOGY),
        entry(Set.of(BrageType.BOOK_OF_ABSTRACTS), NvaType.BOOK_OF_ABSTRACTS),
        entry(Set.of(BrageType.CHAPTER), NvaType.CHAPTER),
        entry(Set.of(BrageType.REPORT_CHAPTER), NvaType.CHAPTER),
        entry(Set.of(BrageType.JOURNAL_ARTICLE), NvaType.JOURNAL_ARTICLE),
        entry(Set.of(BrageType.ARTICLE), NvaType.JOURNAL_ARTICLE),
        entry(Set.of(BrageType.JOURNAL_ISSUE), NvaType.JOURNAL_ISSUE),
        entry(Set.of(BrageType.PROFESSIONAL_ARTICLE), NvaType.PROFESSIONAL_ARTICLE),
        entry(Set.of(BrageType.DATASET), NvaType.DATASET),
        entry(Set.of(BrageType.DATA_SET), NvaType.DATASET),
        entry(Set.of(BrageType.OTHERS), NvaType.REPORT),
        entry(Set.of(BrageType.OTHER), NvaType.REPORT),
        entry(Set.of(BrageType.REPORT), NvaType.REPORT),
        entry(Set.of(BrageType.NOTES), NvaType.REPORT),
        entry(Set.of(BrageType.POSTER), NvaType.CONFERENCE_POSTER),
        entry(Set.of(BrageType.NON_FICTION_CHAPTER), NvaType.CHAPTER),
        entry(Set.of(BrageType.PRESENTATION), NvaType.LECTURE),
        entry(Set.of(BrageType.RESEARCH_REPORT), NvaType.RESEARCH_REPORT),
        entry(Set.of(BrageType.BACHELOR_THESIS), NvaType.BACHELOR_THESIS),
        entry(Set.of(BrageType.MASTER_THESIS), NvaType.MASTER_THESIS),
        entry(Set.of(BrageType.SPECIAL_THESIS), NvaType.MASTER_THESIS),
        entry(Set.of(BrageType.DOCTORAL_THESIS), NvaType.DOCTORAL_THESIS),
        entry(Set.of(BrageType.STUDENT_PAPER), NvaType.STUDENT_PAPER),
        entry(Set.of(BrageType.WORKING_PAPER), NvaType.WORKING_PAPER),
        entry(Set.of(BrageType.STUDENT_PAPER_OTHERS), NvaType.STUDENT_PAPER_OTHERS),
        entry(Set.of(BrageType.STUDENT_THESIS_OTHER), NvaType.STUDENT_PAPER_OTHERS),
        entry(Set.of(BrageType.DESIGN_PRODUCT), NvaType.DESIGN_PRODUCT),
        entry(Set.of(BrageType.CHRONICLE), NvaType.MEDIA_FEATURE_ARTICLE),
        entry(Set.of(BrageType.FEATURE_ARTICLE), NvaType.MEDIA_FEATURE_ARTICLE),
        //        entry(Set.of(BrageType.SOFTWARE), NvaType.SOFTWARE),
        //        entry(Set.of(BrageType.RECORDING_ORAL), NvaType.RECORDING_ORAL),
        entry(Set.of(BrageType.LECTURE), NvaType.LECTURE),
        entry(Set.of(BrageType.RECORDING_MUSICAL), NvaType.RECORDING_MUSICAL),
        entry(Set.of(BrageType.PLAN_OR_BLUEPRINT), NvaType.PLAN_OR_BLUEPRINT),
        entry(Set.of(BrageType.ARCHITECTURE), NvaType.PLAN_OR_BLUEPRINT),
        entry(Set.of(BrageType.MAP), NvaType.MAP),
        entry(Set.of(BrageType.INTERVIEW), NvaType.INTERVIEW),
        entry(Set.of(BrageType.PRESENTATION_OTHER), NvaType.PRESENTATION_OTHER),
        entry(Set.of(BrageType.PERFORMING_ARTS), NvaType.PERFORMING_ARTS),
        entry(Set.of(BrageType.READER_OPINION), NvaType.READER_OPINION),
        entry(Set.of(BrageType.VISUAL_ARTS), NvaType.VISUAL_ARTS),
        entry(Set.of(BrageType.TEXTBOOK), NvaType.TEXTBOOK),
        entry(Set.of(BrageType.OTHER_TYPE_OF_REPORT), NvaType.REPORT),
        entry(Set.of(BrageType.CONFERENCE_OBJECT), NvaType.CONFERENCE_REPORT),
        entry(Set.of(BrageType.CONFERENCE_ABSTRACT), NvaType.CONFERENCE_REPORT),
        entry(Set.of(BrageType.CONFERENCE_REPORT), NvaType.CONFERENCE_REPORT),
        entry(Set.of(BrageType.CONFERENCE_PAPER), NvaType.CONFERENCE_REPORT),
        entry(Set.of(BrageType.CONFERENCE_LECTURE), NvaType.CONFERENCE_LECTURE),
        entry(Set.of(BrageType.CONFERENCE_POSTER), NvaType.CONFERENCE_POSTER),
        entry(Set.of(BrageType.EXHIBITION_CATALOGUE), NvaType.EXHIBITION_CATALOGUE),
        entry(Set.of(BrageType.LITERARY_ARTS), NvaType.LITERARY_ARTS),
        entry(Set.of(BrageType.EDITORIAL), NvaType.EDITORIAL),
        entry(Set.of(BrageType.POPULAR_SCIENCE_MONOGRAPH), NvaType.POPULAR_SCIENCE_MONOGRAPH),
        entry(Set.of(BrageType.FILM), NvaType.FILM));

    public static String convertBrageTypeToNvaType(Set<String> inputTypes) {
        try {
            return mapToNvaTypeIfMappable(inputTypes);
        } catch (Exception e) {
            return mapToAnyMappableNvaTypeWhenUnmappableTypePair(inputTypes);
        }
    }

    public static boolean hasValidType(String inputType) {
        var brageType = convertToBrageType(inputType);
        if (nonNull(brageType)) {
            return TYPE_MAP.containsKey(Collections.singleton(brageType));
        } else {
            return false;
        }
    }

    public static Set<BrageType> getBrageTypeForCorrespondingNvaType(NvaType type) {
        for (Entry<Set<BrageType>, NvaType> entry : TYPE_MAP.entrySet()) {
            if (entry.getValue().equals(type)) {
                return entry.getKey();
            }
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static String mapToAnyMappableNvaTypeWhenUnmappableTypePair(Set<String> inputTypes) {
        var brageTypes = convertToBrageTypes(inputTypes);
        if (brageTypes.isEmpty()) {
            return null;
        } else {
            for (BrageType type : brageTypes) {
                if (hasValidType(type.toString())) {
                    logger.error(String.valueOf(new ErrorDetails(MULTIPLE_UNMAPPABLE_TYPES, inputTypes)));
                    return TYPE_MAP.get(Collections.singleton(type)).getValue();
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private static Set<BrageType> convertToBrageTypes(Set<String> values) {
        var brageTypesFromOriginalNvaTypes = values.stream()
                                                 .map(TypeMapper::format)
                                                 .map(NvaType::fromValue)
                                                 .filter(Objects::nonNull)
                                                 .map(TypeMapper::getBrageTypeForCorrespondingNvaType)
                                                 .filter(Objects::nonNull)
                                                 .flatMap(Set::stream)
                                                 .collect(toSet());
        var brageTypes = values.stream()
                             .map(BrageType::fromValue)
                             .filter(Objects::nonNull)
                             .collect(toSet());
        if (brageTypes.size() > brageTypesFromOriginalNvaTypes.size()) {
            return brageTypes;
        } else {
            return Stream.concat(brageTypesFromOriginalNvaTypes.stream(), brageTypes.stream())
                       .collect(toSet());
        }
    }

    private static String format(String value) {
        return value.replaceAll("(\n)|(\b)|(\u200b)|(\t)", StringUtils.EMPTY_STRING);
    }

    public static String mapToNvaTypeIfMappable(Set<String> inputTypes) {
        Set<BrageType> brageTypes = convertToBrageTypes(inputTypes);
        var nvaType = TYPE_MAP.get(Set.copyOf(brageTypes));
        if (isNull(nvaType) && brageTypes.size() >= 2) {
            for (BrageType type : brageTypes) {
                if (hasValidType(type.getValue())) {
                    return TYPE_MAP.get(Collections.singleton(type)).getValue();
                }
                if (isNvaType(type)) {
                    return type.getValue();
                }
            }
        }
        if (nonNull(nvaType)) {
            return nvaType.getValue();
        } else {
            return null;
        }
    }

    private static boolean isNvaType(BrageType type) {
        return nonNull(NvaType.fromValue(type.getValue()));
    }

    private static BrageType convertToBrageType(String brageType) {
        return BrageType.fromValue(brageType);
    }
}
