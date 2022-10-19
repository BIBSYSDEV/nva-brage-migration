package no.sikt.nva.scrapers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.List;
import no.sikt.nva.scrapers.TypeMapper.NvaType;
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
}
