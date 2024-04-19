package no.sikt.nva.scrapers.embargo;

import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import nva.commons.core.SingletonCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerAddressResolver {

    public static final Set<String> IGNORED_CUSTOMERS = Set.of("ffi");
    private static final Logger logger = LoggerFactory.getLogger(CustomerAddressResolver.class);
    private static final char SEPARATOR = ';';
    private static final String CUSTOMER_ADDRESSES = "customer_address.csv";

    private final List<CustomerAddress> customerAddresses;

    public CustomerAddressResolver() {
        this.customerAddresses = getCustomerAddresses();
    }

    public String getAddressForCustomer(String customer) {
        if (IGNORED_CUSTOMERS.stream().anyMatch(ignoredCustomer -> ignoredCustomer.equalsIgnoreCase(customer))) {
            return customer;
        }
        return customerAddresses.stream()
                   .filter(customerAddress -> customerAddress.getCustomer().equals(customer))
                   .map(CustomerAddress::getAdress)
                   .collect(SingletonCollector.collect());
    }

    private static List<CustomerAddress> getCustomerAddresses() {
        try (var inputStream = Thread.currentThread().getContextClassLoader()
                                   .getResourceAsStream(CUSTOMER_ADDRESSES);
            var bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            var microJournal = new CsvToBeanBuilder<CustomerAddress>(bufferedReader)
                                   .withSeparator(SEPARATOR)
                                   .withType(CustomerAddress.class)
                                   .build();
            return microJournal.parse();
        } catch (IOException e) {
            logger.error("Could not resolve customer addresses");
            throw new RuntimeException(e);
        }
    }
}
