package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Collections;
import java.util.List;
import no.sikt.nva.TypeMapper.Type;
import no.sikt.nva.exceptions.DublinCoreException;
import org.junit.jupiter.api.Test;

public class TypeMapperTest {



    @Test
    void shouldMapSingleWordTypesToNvaType() {
        var expectedNvaType = Type.Others.getNvaType();
        var types = Collections.singletonList(Type.Others.getType());
        var actualNvaType = TypeMapper.toNvaType(types);
        assertThat(actualNvaType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapTypesContainingManyWordsToNvaType() {
        var expectedNvaType = Type.Researchreport.getNvaType();
        var types = Collections.singletonList(Type.Researchreport.getType());
        var actualNvaType = TypeMapper.toNvaType(types);
        assertThat(actualNvaType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldMapTypesContainingManyTypes() {
        var expectedNvaType = Type.Vitenskapeligkapittel.getType();
        var types = List.of(Type.Chapter.getType(), Type.PeerReviewed.getType());
        var actualType = TypeMapper.toNvaType(types);
        assertThat(actualType, is(equalTo(expectedNvaType)));
    }

    @Test
    void shouldThrowExceptionIfCombinationOfInvalidTypes() {
        var types = List.of(Type.Journalarticle.getType(), Type.Others.getType());
        assertThrows(DublinCoreException.class, () -> TypeMapper.toNvaType(types)) ;

    }

    @Test
    void shouldThrowExceptionIfTypeDoesNotExist() {
        var types = List.of("SomeType");
        assertThrows(DublinCoreException.class, () -> TypeMapper.toNvaType(types)) ;

    }
}
