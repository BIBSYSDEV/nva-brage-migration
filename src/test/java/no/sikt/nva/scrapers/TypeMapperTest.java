package no.sikt.nva.scrapers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.NvaType;
import org.junit.jupiter.api.Test;

public class TypeMapperTest {

    @Test
    void shouldMapSingleWordTypesToNvaType() {
        var expectedNvaType = NvaType.BOOK.getValue();
        var actualNvaType = TypeMapper.convertBrageTypeToNvaType(List.of("Book"));
        assertThat(actualNvaType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapTypesContainingManyTypes() {
        var expectedNvaType = NvaType.SCIENTIFIC_CHAPTER.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(List.of("Chapter", "Peer Reviewed"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapBachelorThesis() {
        var expectedNvaType = NvaType.BACHELOR_THESIS.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(List.of("Bachelor thesis"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapStudentPaperOthers() {
        var expectedNvaType = NvaType.STUDENT_PAPER_OTHERS.getValue();
        var actualType = TypeMapper.convertBrageTypeToNvaType(List.of("Student paper, others"));
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }
}
