package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import static java.util.Objects.isNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import no.sikt.nva.model.BrageType;
import no.sikt.nva.model.ErrorDetails;
import no.sikt.nva.model.ErrorDetails.Error;
import no.sikt.nva.model.NvaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TypeMapper {

    private static final Logger logger = LoggerFactory.getLogger(TypeMapper.class);
    private static final Map<Set<BrageType>, NvaType> TYPE_MAP = Map.ofEntries(
        entry(Set.of(BrageType.BOOK, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_MONOGRAPH),
        entry(Set.of(BrageType.CHAPTER, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_CHAPTER),
        entry(Set.of(BrageType.JOURNAL_ARTICLE, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_ARTICLE),
        entry(Set.of(BrageType.BOOK), NvaType.BOOK),
        entry(Set.of(BrageType.CHAPTER), NvaType.CHAPTER),
        entry(Set.of(BrageType.JOURNAL_ARTICLE), NvaType.JOURNAL_ARTICLE),
        entry(Set.of(BrageType.DATASET), NvaType.DATASET),
        entry(Set.of(BrageType.OTHERS), NvaType.OTHERS),
        entry(Set.of(BrageType.REPORT), NvaType.REPORT),
        entry(Set.of(BrageType.RESEARCH_REPORT), NvaType.RESEARCH_REPORT),
        entry(Set.of(BrageType.BACHELOR_THESIS), NvaType.BACHELOR_THESIS),
        entry(Set.of(BrageType.MASTER_THESIS), NvaType.MASTER_THESIS),
        entry(Set.of(BrageType.DOCTORAL_THESIS), NvaType.DOCTORAL_THESIS),
        entry(Set.of(BrageType.STUDENT_PAPER), NvaType.STUDENT_PAPER),
        entry(Set.of(BrageType.WORKING_PAPER), NvaType.WORKING_PAPER),
        entry(Set.of(BrageType.STUDENT_PAPER_OTHERS), NvaType.STUDENT_PAPER_OTHERS),
        entry(Set.of(BrageType.DESIGN_PRODUCT), NvaType.DESIGN_PRODUCT),
        entry(Set.of(BrageType.CHRONICLE), NvaType.CHRONICLE),
        entry(Set.of(BrageType.SOFTWARE), NvaType.SOFTWARE),
        entry(Set.of(BrageType.LECTURE), NvaType.LECTURE),
        entry(Set.of(BrageType.RECORDING_MUSICAL), NvaType.RECORDING_MUSICAL),
        entry(Set.of(BrageType.RECORDING_ORAL), NvaType.RECORDING_ORAL),
        entry(Set.of(BrageType.PLAN_OR_BLUEPRINT), NvaType.PLAN_OR_BLUEPRINT),
        entry(Set.of(BrageType.MAP), NvaType.MAP)
    );

    public static String convertBrageTypeToNvaType(List<String> brageTypesAsString) {
        List<BrageType> brageTypes = convertToBrageTypes(brageTypesAsString);
        try {
            return mapToNvaTypeIfMappable(brageTypesAsString, brageTypes);
        } catch (Exception e) {
            return mapToAnyMappableNvaTypeWhenUnmappableTypePair(brageTypesAsString, brageTypes);
        }
    }

    public static boolean hasValidType(String brageType) {
        var typeFromMap = convertToBrageType(brageType);
        return TYPE_MAP.containsKey(Collections.singleton(typeFromMap));
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static String mapToAnyMappableNvaTypeWhenUnmappableTypePair(List<String> brageTypesAsString,
                                                                        List<BrageType> brageTypes) {
        for (BrageType type : brageTypes) {
            if (hasValidType(type.toString())) {
                logger.error(String.valueOf(new ErrorDetails(Error.MANY_UNMAPPABLE_TYPES, brageTypesAsString)));
                return TYPE_MAP.get(Collections.singleton(type)).getValue();
            } else {
                logger.error(String.valueOf(new ErrorDetails(Error.INVALID_TYPE, brageTypesAsString)));
                return null;
            }
        }
        logger.error(String.valueOf(new ErrorDetails(Error.INVALID_TYPE, brageTypesAsString)));
        return null;
    }

    @NotNull
    private static List<BrageType> convertToBrageTypes(List<String> brageTypesAsString) {
        return brageTypesAsString
                   .stream()
                   .map(BrageType::fromValue)
                   .collect(Collectors.toList());
    }

    @Nullable
    private static String mapToNvaTypeIfMappable(List<String> brageTypesAsString, List<BrageType> brageTypes) {
        var nvaType = TYPE_MAP.get(Set.copyOf(brageTypes));
        if (isNull(nvaType) && brageTypes.size() >= 2) {
            for (BrageType type : brageTypes) {
                if (hasValidType(type.toString())) {
                    return TYPE_MAP.get(Collections.singleton(type)).getValue();
                }
            }
        }
        if (Objects.nonNull(nvaType)) {
            return nvaType.getValue();
        } else {
            logger.error(String.valueOf(new ErrorDetails(Error.INVALID_TYPE, brageTypesAsString)));
            return null;
        }
    }

    private static BrageType convertToBrageType(String brageType) {
        return BrageType.fromValue(brageType);
    }
}
