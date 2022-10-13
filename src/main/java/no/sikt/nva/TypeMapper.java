package no.sikt.nva;

import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
import nva.commons.core.StringUtils;

public final class TypeMapper {

    public static String toNvaType(List<String> types) {

        var strippedTypes = types.stream()
                                .map(StringUtils::removeWhiteSpaces)
                                .collect(Collectors.toList());

        if (containsOneType(strippedTypes)) {
            return convertToNvaTypeIfOneType(strippedTypes);
        }
        if (containsTwoTypes(strippedTypes)) {
            return convertToNvaTypeIfTwoTypes(strippedTypes);
        }
        throw new DublinCoreException("Invalid type");
    }

    private static String convertToNvaTypeIfOneType(List<String> types) {
        try {
            var type = getFirstType(types);
            var strippedType = StringUtils.removeWhiteSpaces(type);
            return Type.valueOf(strippedType).nvaType;
        } catch (Exception e) {
            throw new DublinCoreException("Types does not exist");
        }
    }

    private static String convertToNvaTypeIfTwoTypes(List<String> types) {
        if (mapsToScientificMonograph(types)) {
            return Type.Vitenskapeligmonografi.type;
        }
        if (mapsToScientificChapter(types)) {
            return Type.Vitenskapeligkapittel.type;
        }
        if (mapsToScientificArticle(types)) {
            return Type.Vitenskapeligartikkel.type;
        }
        throw new DublinCoreException("Invalid type");
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

    private static boolean containsTwoTypes(List<String> types) {
        return types.size() == 2;
    }

    private static String getFirstType(List<String> types) {
        return types.get(0);
    }

    private static boolean containsOneType(List<String> types) {
        return types.size() == 1;
    }

    public enum Type {

        Book("Book", "Faglig Monografi"),
        Chapter("Chapter", "Faglig kapittel"),
        Dataset("Dataset", "Datasett"),
        Journalarticle("Journal article", "Fagartikkel"),
        Others("Others", "Annen rapport"),
        Report("Report", "Rapport"),
        Researchreport("Research report", "Forskningsrapport"),
        PeerReviewed("Peer Reviewed"),
        Vitenskapeligmonografi("Vitenskapelig monografi"),
        Vitenskapeligkapittel("Vitenskapelig kapittel"),
        Vitenskapeligartikkel("Vitenskapelig artikkel");
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
