package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.Test;

public class UnZipperTest {

    public static final String TEST_ROOT_DIRECTORY_NAME = "testWithDirectory";
    private static final String TEST_DESTINATION_DIRECTORY_NAME = "tmp";
    private static final String TEST_FILE_NAME = "testFile.txt";
    private final UnZipper unZipper = new UnZipper();

    @Test
    void shouldUnzipFileAndKeepOriginalStructureAndNames() {
        unZipper.unzip(IoUtils.inputStreamFromResources("testWithDirectory.zip"), new File("tmp"));
        var actualRootDirectory = getActualRootDirectory();
        var actualFileName = getFile();

        assertThat(actualFileName, is(equalTo(TEST_FILE_NAME)));
        assertThat(actualRootDirectory, is(equalTo(TEST_ROOT_DIRECTORY_NAME)));
    }

    private String getActualRootDirectory() {
        return Stream.of(Objects.requireNonNull(new File(TEST_DESTINATION_DIRECTORY_NAME).listFiles()))
                   .filter(File::isDirectory)
                   .map(File::getName)
                   .collect(Collectors.toList()).get(0);
    }

    private File getContentInsideDirectory() {
        return Stream.of(Objects.requireNonNull(new File(TEST_DESTINATION_DIRECTORY_NAME).listFiles()))
                   .filter(File::isDirectory)
                   .collect(Collectors.toList())
                   .get(0);
    }

    private String getFile() {
        return Objects.requireNonNull(Objects.requireNonNull(getContentInsideDirectory().listFiles())[1]
                                          .listFiles())[0].getName();
    }
}
