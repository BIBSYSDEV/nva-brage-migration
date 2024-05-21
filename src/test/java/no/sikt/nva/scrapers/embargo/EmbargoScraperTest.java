package no.sikt.nva.scrapers.embargo;

import static no.sikt.nva.scrapers.embargo.EmbargoParser.PERMANENTLY_LOCKED;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.net.ssl.SSLSession;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent.BundleType;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.model.Embargo;
import no.sikt.nva.utils.FakeOnlineEmbargoChecker;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatcher;

public class EmbargoScraperTest {

    public static final String HANDLE = "https://hdl.handle.net/11250/2683076";
    public static final String FILENAME = "Simulated"
                                          + " precipitation fields with variance consistent interpolation.pdf";
    public static final String DATE = "2023-10-01";
    public static final String TEST_FILE_LOCATION = "src/test/resources/FileEmbargo.txt";
    public static final String EMPTY_EMBARGO_FILE = "src/test/resources/EmptyEmbargoFile.txt";
    public static final String TEST_FILE_LOCATION_V2 = "src/test/resources/FileEmbargoV2.txt";
    public static final String TEST_FILE_LOCATION_V3 = "src/test/resources/FileEmbargoV3.txt";
    private static final String EMBARGO_FILE_WITH_ONLY_IGNORED_FILES = "src/test/resources"
                                                                       + "/FileEmbargoWithOnlyIgnoredFiles.txt";
    private static final String EMBARGO_WITH_DISTANT_DATE_TXT = "src/test/resources/FileEmbargo_with_distant_date.txt";
    private static final String EMBARGO_WITH_SUMMER_AND_WINDER_TIME_EMBARGO = "src/test/resources"
                                                                              + "/FileEmbargo_with_daylight_saving"
                                                                              + ".txt";

    @Test
    void shouldIgnoreDefaultRowsOfEmbargoFile() {
        var actualEmbargos =
            Objects.requireNonNull(EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION))).get(HANDLE);
        assertThat(actualEmbargos, hasSize(3));
    }

    @Test
    void shouldConvertPrettifiedEmbargoFileToEmbargoes() {
        var expectedEmbargo = new Embargo(HANDLE, FILENAME, DATE);
        var actualEmbargos =
            Objects.requireNonNull(EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION_V3))).get(HANDLE);

        assertThat(actualEmbargos, hasSize(3));
        assertThat(actualEmbargos, hasItem(expectedEmbargo));
    }

    @Test
    void shouldExtractEmbargoWithPdfFile() {
        var expectedEmbargo = new Embargo(HANDLE, FILENAME, DATE);
        var actualEmbargos =
            Objects.requireNonNull(EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION))).get(HANDLE);
        assertThat(actualEmbargos, hasItem(expectedEmbargo));
    }

    @Test
    void shouldExtractEmbargosWithDifferentDelimiter() {
        var expectedEmbargo = new Embargo(HANDLE, FILENAME, DATE);
        var actualEmbargo = Objects.requireNonNull(EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION_V2)))
                                .get(HANDLE);
        assertThat(actualEmbargo, hasItem(expectedEmbargo));
    }

    @Test
    void shouldLogNonDetectedEmbargos() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var embargos = EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION));
        EmbargoParser.logNonEmbargosDetected(embargos);
        assertThat(appender.getMessages(), containsString("Embargo file not found: "));
    }

    @Test
    void shouldNotLogCristinMetadataZip() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var embargos = EmbargoScraper.getEmbargoes(new File(EMBARGO_FILE_WITH_ONLY_IGNORED_FILES));
        EmbargoParser.logNonEmbargosDetected(embargos);
        assertThat(appender.getMessages(), not(containsString("Embargo file not found: ")));
    }

    @Test
    void shouldNotLogDetectedEmbargos() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var embargos = EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION));
        var record = new Record();
        record.setId(UriWrapper.fromUri("https://hdl.handle.net/11250/2683076").getUri());
        record.setContentBundle(contentBundleWithFileNameFromEmbargo(
            List.of("Simulated precipitation fields with variance consistent interpolation.pdf")));
        var updatedRecord = EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(record, embargos,
                                                                                 new FakeOnlineEmbargoChecker());
        assertThat(appender.getMessages(), not(containsString("Embargo file not found: ")));
        assertThat(updatedRecord.getContentBundle().getContentFiles(), hasSize(1));
        var actualContentFile = updatedRecord.getContentBundle().getContentFiles().get(0);
        assertThat(actualContentFile.getEmbargoDate(), is(equalTo(Instant.parse("2023-09-30T22:00:00Z"))));
    }

    @Test
    void shouldSetEmbargoOnMultipleContentFilesIfNecessary() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var embargos = EmbargoScraper.getEmbargoes(new File(EMBARGO_WITH_DISTANT_DATE_TXT));
        var record = new Record();
        record.setId(UriWrapper.fromUri("https://hdl.handle.net/11250/2683076").getUri());
        record.setContentBundle(contentBundleWithFileNameFromEmbargo(
            List.of("Simulated precipitation fields with variance consistent interpolation.pdf",
                    "My super secret file.pdf")));
        var updatedRecord = EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(record, embargos,
                                                                                 new FakeOnlineEmbargoChecker());
        assertThat(appender.getMessages(), not(containsString("Embargo file not found: ")));

        assertThat(updatedRecord.getContentBundle().getContentFiles(), hasSize(2));
        assertThat(updatedRecord.getContentBundle().getContentFiles().get(0).getEmbargoDate(),
                   is(equalTo(Instant.parse("2023-09-30T22:00:00Z"))));
        assertThat(updatedRecord.getContentBundle().getContentFiles().get(1).getEmbargoDate(),
                   is(equalTo(Instant.parse("9999-09-30T22:00:00Z"))));
    }

    @Test
    void shouldHonorDayTimeSavingWhenSettingEmbargoDate() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var embargos = EmbargoScraper.getEmbargoes(new File(EMBARGO_WITH_SUMMER_AND_WINDER_TIME_EMBARGO));
        var record = new Record();
        record.setId(UriWrapper.fromUri("https://hdl.handle.net/11250/2683076").getUri());
        record.setContentBundle(contentBundleWithFileNameFromEmbargo(
            List.of("Simulated precipitation fields with variance consistent interpolation.pdf",
                    "Some name.pdf.jpg")));
        var updatedRecord = EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(record, embargos,
                                                                                 new FakeOnlineEmbargoChecker());
        assertThat(appender.getMessages(), not(containsString("Embargo file not found: ")));

        assertThat(updatedRecord.getContentBundle().getContentFiles(), hasSize(2));
        assertThat(updatedRecord.getContentBundle().getContentFiles().get(0).getEmbargoDate(),
                   is(equalTo(Instant.parse("2023-05-31T22:00:00Z"))));
        assertThat(updatedRecord.getContentBundle().getContentFiles().get(1).getEmbargoDate(),
                   is(equalTo(Instant.parse("2023-11-30T23:00:00Z"))));
    }

    @Test
    void shouldCreateEmptyEmbargoListWhenEmptyEmbargoFile() {
        var embargos = EmbargoScraper.getEmbargoes(new File(EMPTY_EMBARGO_FILE));
        assertThat(embargos.size(), is(0));
    }

    @Test
    void shouldConvertEmbargoDateToInstant() {
        var embargo = new Embargo(HANDLE, FILENAME, DATE);
        assertThat(embargo.getDateAsInstant(), is(instanceOf(Instant.class)));
    }

    @Test
    void shouldConvertFiveDigitEmbargoYearTo9999() {
        var embargo = new Embargo(HANDLE, FILENAME, "20230-10-01");

        var dateAsInstant = embargo.getDateAsInstant();
        assertThat(dateAsInstant, is(instanceOf(Instant.class)));
        assertThat(dateAsInstant.atZone(ZoneId.systemDefault()).getYear(), is(equalTo(9999)));
    }

    @ParameterizedTest
    @ValueSource(ints = {302, 401, 404})
    void shouldSetFileToEmbargoWhenItIsNotPossibleToQueryFileOnline(int statusCode) throws IOException,
                                                                                      InterruptedException {
        var someHandle = "https://hdl.handle.net/1234/12345";
        var filename = "somefile.pdf";
        var record = new Record();
        var embargos = new HashMap<String, List<Embargo>>();
        record.setId(UriWrapper.fromUri(someHandle).getUri());
        record.setContentBundle(new ResourceContent(List.of(new ContentFile(filename, BundleType.ORIGINAL,
                                                                            randomString(),
                                                                            UUID.randomUUID(),
                                                                            License.fromBrageLicense(
                                                                                BrageLicense.CC_BY),
                                                                            null))));
        var httpClient = mock(HttpClient.class);
        var onlineEmbargoChecker = new OnlineEmbargoCheckerImpl(httpClient);
        onlineEmbargoChecker.calculateCustomerAddress("ntnu");
        onlineEmbargoChecker.setOutputDirectory("someoutputpath");
        mockErrorResponse(httpClient, statusCode);
        var recordWithEmbargoOnFile = EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(record, embargos,
                                                                                           onlineEmbargoChecker);
        assertThat(recordWithEmbargoOnFile.getContentBundle().getContentFiles().get(0).getEmbargoDate(),
                   is(equalTo(PERMANENTLY_LOCKED)));
    }



    @Test
    void shouldNotSetEmbargoWhenFileIsOpenOnline() throws IOException, InterruptedException {
        var someHandle = "https://hdl.handle.net/1234/12345";
        var filename = "some_file.pdf";
        var record = new Record();
        var embargos = new HashMap<String, List<Embargo>>();
        record.setId(UriWrapper.fromUri(someHandle).getUri());
        record.setContentBundle(new ResourceContent(List.of(new ContentFile(filename, BundleType.ORIGINAL,
                                                                            randomString(),
                                                                            UUID.randomUUID(),
                                                                            License.fromBrageLicense(
                                                                                BrageLicense.CC_BY),
                                                                            null))));
        var httpClient = mock(HttpClient.class);
        var onlineEmbargoChecker = new OnlineEmbargoCheckerImpl(httpClient);
        onlineEmbargoChecker.calculateCustomerAddress("ntnu");
        onlineEmbargoChecker.setOutputDirectory("someoutputpath");
        mock200Response(httpClient);
        var recordWithEmbargoOnFile = EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(record, embargos,
                                                                                           onlineEmbargoChecker);
        assertThat(recordWithEmbargoOnFile.getContentBundle().getContentFiles().get(0).getEmbargoDate(),
                   is(nullValue()));
    }

    @Test
    void shouldNotCheckFFIforOnlineEmbargo() throws IOException, InterruptedException {
        var someHandle = "https://hdl.handle.net/1234/12345";
        var filename = "some_file.pdf";
        var record = new Record();
        var embargos = new HashMap<String, List<Embargo>>();
        record.setId(UriWrapper.fromUri(someHandle).getUri());
        record.setContentBundle(new ResourceContent(List.of(new ContentFile(filename, BundleType.ORIGINAL,
                                                                            randomString(),
                                                                            UUID.randomUUID(),
                                                                            License.fromBrageLicense(
                                                                                BrageLicense.CC_BY),
                                                                            null))));
        var httpClient = mock(HttpClient.class);
        var onlineEmbargoChecker = new OnlineEmbargoCheckerImpl(httpClient);
        onlineEmbargoChecker.calculateCustomerAddress("ffi");
        onlineEmbargoChecker.setOutputDirectory("someoutputpath");
        mockErrorResponse(httpClient, 302);
        var recordWithEmbargoOnFile = EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(record, embargos,
                                                                                           onlineEmbargoChecker);
        assertThat(recordWithEmbargoOnFile.getContentBundle().getContentFiles().get(0).getEmbargoDate(),
                   is(nullValue()));

    }

    @Test
    void shouldUrlEncodeFileNameParameter() throws IOException, InterruptedException {
        var someHandle = "https://hdl.handle.net/11250/2736303";
        var filename = "NF_rapport 11_2018 Redusere marint avfall fra fiskeflåten (REMAFISK).pdf";
        var record = new Record();
        var embargos = new HashMap<String, List<Embargo>>();
        record.setId(UriWrapper.fromUri(someHandle).getUri());
        record.setContentBundle(new ResourceContent(List.of(new ContentFile(filename, BundleType.ORIGINAL,
                                                                            randomString(),
                                                                            UUID.randomUUID(),
                                                                            License.fromBrageLicense(
                                                                                BrageLicense.CC_BY),
                                                                            null))));
        var httpClient = mock(HttpClient.class);
        var onlineEmbargoChecker = new OnlineEmbargoCheckerImpl(httpClient);
        onlineEmbargoChecker.calculateCustomerAddress("nforsk");
        onlineEmbargoChecker.setOutputDirectory("someoutputpath");
        mock200Response(httpClient);
        EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(record, embargos,
                                                             onlineEmbargoChecker);
        verify(httpClient, times(1)).send(argThat(matchesExpectedUrl("NF_rapport%2011_2018%20Redusere%20marint%20avfall%20fra%20fiskefla%CC%8Aten%20%28REMAFISK%29.pdf")), any(BodyHandler.class));
    }

    private ArgumentMatcher<HttpRequest> matchesExpectedUrl(String url) {
        return argument -> argument.uri().toString().contains(url);
    }

    private void mockErrorResponse(HttpClient httpClient, int statusCode) throws IOException, InterruptedException {
        doReturn(new HttpResponse<String>() {
            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public String body() {
                return "";
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public Version version() {
                return null;
            }
        }).when(httpClient).send(any(), any());
    }


    private void mock200Response(HttpClient httpClient) throws IOException, InterruptedException {
        doReturn(new HttpResponse<String>() {
            @Override
            public int statusCode() {
                return 200;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public String body() {
                return "";
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public Version version() {
                return null;
            }
        }).when(httpClient).send(any(), any());
    }

    private ResourceContent contentBundleWithFileNameFromEmbargo(List<String> filenames) {
        var contentfiles = filenames
                               .stream()
                               .map(this::createContentFile)
                               .collect(Collectors.toList());
        return new ResourceContent(contentfiles);
    }

    private ContentFile createContentFile(String filename) {
        var contentFile = new ContentFile();
        contentFile.setFilename(filename);
        return contentFile;
    }
}
