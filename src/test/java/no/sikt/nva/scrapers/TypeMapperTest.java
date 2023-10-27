package no.sikt.nva.scrapers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.Set;
import no.sikt.nva.brage.migration.common.model.NvaType;
import org.junit.jupiter.api.Test;

public class TypeMapperTest {

    @Test
    void shouldMapSingleWordTypesToNvaType() {
        var expectedNvaType = NvaType.BOOK.getValue();
        var actualNvaType = TypeMapper.convertBrageTypeToNvaType(Set.of("Book"));
        assertThat(actualNvaType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapTypesContainingManyTypes() {
        var expectedNvaType = NvaType.SCIENTIFIC_CHAPTER.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(Set.of("Chapter", "Peer Reviewed"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapBachelorThesis() {
        var expectedNvaType = NvaType.BACHELOR_THESIS.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(Set.of("Bachelor thesis"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapStudentPaperOthers() {
        var expectedNvaType = NvaType.STUDENT_PAPER_OTHERS.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(Set.of("Student paper, others"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapOriginalNvaTypeToNvaType() {
        var expectedNvaType = NvaType.JOURNAL_ARTICLE.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(Set.of("JournalArticle"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapAnthologyToNvaType() {
        var expectedNvaType = NvaType.ANTHOLOGY.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(Set.of("Anthology"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void typeMapperShouldNotBeCaseSensitive() {
        var expectedNvaType = NvaType.REPORT.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(Set.of("Other report\t"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void typeMapperShouldRemoveSpecialCharacters() {
        var expectedNvaType = NvaType.REPORT.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(Set.of("\nOther\b \u200breport\t"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapOriginalNvaTypeAndBrageTypeToNvaType() {
        var expectedNvaType = NvaType.JOURNAL_ARTICLE.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(Set.of("JournalArticle", "Journal article"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapOriginalNvaTypeAndUnsupportedTypeToNvaType() {
        var expectedNvaType = NvaType.JOURNAL_ARTICLE.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(Set.of("Article", "Journal article"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }
}
