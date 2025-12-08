package no.sikt.nva.scrapers.embargo;

import static no.sikt.nva.scrapers.embargo.CustomerAddressResolver.IGNORED_CUSTOMERS;
import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnlineEmbargoCheckerImpl implements OnlineEmbargoChecker {

    private static final Logger logger = LoggerFactory.getLogger(OnlineEmbargoCheckerImpl.class);
    private static final String WARNING_FORMATTED = "%s , %s\n";
    private static final String FILES_LOCKED_DUE_TO_ONLINE_CHECK_FAILS =
        "LockedDuringOnlineCheck.csv";
    private static final int REDIRECT = 302;
    private static final int NOT_FOUND = 404;
    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final Set<Integer> SHOULD_BE_LOCKED_STATUS_CODES = Set.of(REDIRECT, NOT_FOUND, UNAUTHORIZED, FORBIDDEN);
    private static final int TOO_MANY_REQUESTS = 429;
    private static final int OK = 200;

    private static final int MAX_RETRIES = 3;
    private static final int WAIT_TIME = 2000;
    private final HttpClient httpClient;

    private String customerAddress;
    private String outputDirectory;

    @JacocoGenerated
    public OnlineEmbargoCheckerImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public OnlineEmbargoCheckerImpl() {
        this(HttpClient.newBuilder()
                 .version(Version.HTTP_2)
                 .followRedirects(Redirect.NEVER)
                 .build());
    }

    @Override
    public boolean fileIsLockedOnline(String handle, String filename) {
        if (customerAddress == null || outputDirectory == null) {
            throw new IllegalArgumentException("CustomerAddress or outputDirectory is null");
        }
        if (IGNORED_CUSTOMERS.contains(customerAddress)) {
            return false;
        }
        var isLockedOnline = checkIfFileIsLockedOnline(handle, filename);
        if (isLockedOnline) {
            writeOnlineEmbargoToFile(handle, filename);
        }
        return isLockedOnline;
    }

    @Override
    public void calculateCustomerAddress(String customer) {
        var customerAddressResolver = new CustomerAddressResolver();
        this.customerAddress = customerAddressResolver.getAddressForCustomer(customer);
    }

    @Override
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void writeOnlineEmbargoToFile(String handle, String filename) {
        try (var write = Files.newBufferedWriter(Path.of(outputDirectory, FILES_LOCKED_DUE_TO_ONLINE_CHECK_FAILS),
                                                 StandardCharsets.UTF_8,
                                                 StandardOpenOption.CREATE,
                                                 StandardOpenOption.APPEND)
        ) {
            write.write(String.format(WARNING_FORMATTED, handle, filename));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    private boolean checkIfFileIsLockedOnline(String handle, String filename) {
        var fullUri = extractFullUri(handle, filename);
        var request = createRequest(fullUri);
        return foundLockedFileOnline(request, fullUri, MAX_RETRIES);
    }

    private URI extractFullUri(String handle, String filename) {
        var handleSplitted = handle.split("/");
        var handlePrefix = handleSplitted[handleSplitted.length - 2];
        var handlePostfix = handleSplitted[handleSplitted.length - 1];
        var uriString = customerAddress + handlePrefix + "/" + handlePostfix + "/" + pathEncodeFilename(filename);
        return attempt(() -> URI.create(uriString)).orElseThrow();
    }

    private String pathEncodeFilename(String filename) {
        return attempt(()-> URLEncoder.encode(filename, StandardCharsets.UTF_8))
                   .map(withUnsupportedPlusCharacter -> withUnsupportedPlusCharacter.replaceAll("\\+", "%20"))
                   .orElseThrow();
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    private boolean foundLockedFileOnline(HttpRequest request, URI fullUri, int retriesLeft) {
        try {
            var response = httpClient.send(request, BodyHandlers.ofString());
            if (!response.uri().getRawPath().equals(request.uri().getRawPath())) {
                return true;
            }
            if (response.statusCode() == OK) {
                return false;
            } else if (SHOULD_BE_LOCKED_STATUS_CODES.contains(response.statusCode())) {
                return true;
            } else if (response.statusCode() == TOO_MANY_REQUESTS) {
                if (retriesLeft > 0) {
                    Thread.sleep(WAIT_TIME);
                    return foundLockedFileOnline(request, fullUri, retriesLeft - 1);
                } else {
                    logger.error("Online check of embargo failed repeatedly for {} with status code {}",
                                 fullUri.toString(), response.statusCode());
                    return false;
                }
            } else {
                logger.error("Online check of embargo failed for {} with status code {}", fullUri.toString(),
                             response.statusCode());
                return false;
            }
        } catch (Exception e) {
            if (retriesLeft > 0) {
                return foundLockedFileOnline(request, fullUri, retriesLeft - 1);
            } else {
                logger.error("Online check of embargo failed for {}", fullUri.toString(), e);
            }
        }
        return false;
    }

    private HttpRequest createRequest(URI fullUri) {
        return HttpRequest.newBuilder()
                   .uri(fullUri)
                   .method("HEAD", HttpRequest.BodyPublishers.noBody())
                   .build();
    }
}
