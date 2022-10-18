package no.sikt.nva;

import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
import nva.commons.core.StringUtils;
import org.apache.commons.lang3.EnumUtils;

public final class TypeMapper {

    public static final String INVALID_TYPE_MESSAGE = "Invalid type";

    public static String toNvaType(List<String> types) {
        if (canBeConverted(types)) {
            var strippedTypes = types.stream()
                                    .map(StringUtils::removeWhiteSpaces)
                                    .collect(Collectors.toList());

            return convertToNvaType(strippedTypes);
        } else {
            throw new DublinCoreException(INVALID_TYPE_MESSAGE);
        }
    }

    public static boolean canBeConverted(List<String> types) {
        return types.stream().allMatch(TypeMapper::isType);
    }

    private static String convertToNvaType(List<String> types) {
        if (containsOneType(types)) {
            return getTypeFromSingleton(types);
        }
        if (containsTwoTypes(types)) {
            return getTypeFromListWithTwoTypes(types);
        }
        throw new DublinCoreException(INVALID_TYPE_MESSAGE);
    }

    private static String getTypeFromListWithTwoTypes(List<String> types) {
        if (mapsToScientificMonograph(types)) {
            return Type.Vitenskapeligmonografi.type;
        }
        if (mapsToScientificChapter(types)) {
            return Type.Vitenskapeligkapittel.type;
        }
        if (mapsToScientificArticle(types)) {
            return Type.Vitenskapeligartikkel.type;
        }
        throw new DublinCoreException(INVALID_TYPE_MESSAGE);
    }

    private static String getTypeFromSingleton(List<String> types) {
        try {
            var type = getTypeFromList(types);
            var strippedType = StringUtils.removeWhiteSpaces(type);
            return Type.valueOf(strippedType).nvaType;
        } catch (Exception e) {
            throw new DublinCoreException(INVALID_TYPE_MESSAGE);
        }
    }

    private static boolean mapsToScientificArticle(List<String> types) {
        return types.contains(String.valueOf(Type.Journalarticle.getType())) && types.contains(
            String.valueOf(Type.PeerReviewed.getType()));
    }

    private static boolean mapsToScientificChapter(List<String> types) {
        return types.contains(String.valueOf(Type.Chapter.type))
               && types.contains(String.valueOf(Type.PeerReviewed));
    }

    private static boolean mapsToScientificMonograph(List<String> types) {
        return types.contains(String.valueOf(Type.Book.type))
               && types.contains(String.valueOf(Type.PeerReviewed));
    }

    private static String getTypeFromList(List<String> types) {
        return types.get(0);
    }

    private static boolean containsTwoTypes(List<String> types) {
        return types.size() == 2;
    }

    private static boolean containsOneType(List<String> types) {
        return types.size() == 1;
    }

    private static boolean isType(String type) {
        return EnumUtils.isValidEnum(Type.class, StringUtils.removeWhiteSpaces(type));
    }

    public enum Type {

        Book("Book", "Faglig Monografi"),
        Chapter("Chapter", "Faglig kapittel"),
        Dataset("Dataset", "Datasett"),
        Journalarticle("Journal article", "Fagartikkel"),
        Others("Others", "Annen rapport"),
        Report("Report", "Rapport"),
        Researchreport("Research report", "Forskningsrapport"),
        Vitenskapeligmonografi("Vitenskapelig monografi"),
        Vitenskapeligkapittel("Vitenskapelig kapittel"),
        Vitenskapeligartikkel("Vitenskapelig artikkel"),
        PeerReviewed("Peer Reviewed");
        private final String type;
        private String nvaType;

        Type(String type, String nvaType) {
            this.nvaType = nvaType;
            this.type = type;
        }

        Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public String getNvaType() {
            return nvaType;
        }
    }
}
