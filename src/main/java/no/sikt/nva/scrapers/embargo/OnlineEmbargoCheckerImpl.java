package no.sikt.nva.scrapers.embargo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnlineEmbargoCheckerImpl implements OnlineEmbargoChecker {

    private static final Logger logger = LoggerFactory.getLogger(OnlineEmbargoCheckerImpl.class);
    private static final String WARNING_FORMATTED = "%s,%s\n";
    private static final String FILES_LOCKED_DUE_TO_ONLINE_CHECK_FAILS =
        "LockedDuringOnlineCheck.csv";
    private static final int TOO_MANY_REQUESTS = 429;
    private static final int OK = 200;
    private static final int REDIRECT = 302;
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
        var handleSplitted = handle.split("/");
        var handlePrefix = handleSplitted[handleSplitted.length - 2];
        var handlePostfix = handleSplitted[handleSplitted.length - 1];
        var fullUri = UriWrapper.fromUri(customerAddress)
                          .addChild(handlePrefix)
                          .addChild(handlePostfix)
                          .addChild(filename)
                          .getUri();
        var request = createRequest(fullUri);
        var isLockedOnline = foundLockedFileOnline(request, fullUri, MAX_RETRIES);
        if (isLockedOnline){
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

    private boolean foundLockedFileOnline(HttpRequest request, URI fullUri, int retriesLeft) {
        try {
            var response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == REDIRECT) {
                return true;
            } else if (response.statusCode() == OK) {
                return false;
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