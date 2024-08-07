package no.sikt.nva.unis;

import static java.util.Objects.isNull;
import static no.sikt.nva.brage.migration.common.model.NvaType.CRISTIN_RECORD;
import static no.sikt.nva.unis.UnisContentValidator.validateAndExtractCristinId;
import static no.sikt.nva.unis.UnisContentValidator.validateAndExtractEmbargo;
import static no.sikt.nva.unis.UnisContentValidator.validateAndExtractFilename;
import static no.sikt.nva.unis.UnisContentValidator.validateAndExtractLicense;
import static no.sikt.nva.unis.UnisContentValidator.validateAndExtractPublisherAuthority;
import static no.sikt.nva.unis.UnisContentValidator.validateAndExtractTitle;
import static no.sikt.nva.unis.UnisContentValidator.validateRow;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import no.sikt.nva.brage.migration.common.model.record.Customer;
import no.sikt.nva.brage.migration.common.model.record.EntityDescription;
import no.sikt.nva.brage.migration.common.model.record.PublisherAuthorityEnum;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.Type;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent.BundleType;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import nva.commons.core.paths.UriWrapper;
import org.apache.poi.ss.usermodel.Row;

public final class UnisContent {

    public static final String DUMMY_HANDLE_THAT_EXIST_FOR_PROCESSING_UNIS
        = "dummy_handle_unis";
    public static final String EMPTY_STRING = "";
    public static final String UNIS_CUSTOMER = "unis";
    private int cristinId;
    private String title;
    private PublisherAuthorityEnum publisherAuthority;
    private Instant embargo;
    private BrageLicense license;
    private String filename;

    private UnisContent() {
    }

    public static UnisContent fromRow(Row row) throws ExcelException, InvalidUnisContentException {
        validateRow(row);
        return UnisContent.builder()
                   .withCristinId(validateAndExtractCristinId(row))
                   .withTitle(validateAndExtractTitle(row))
                   .withPublisherAuthority(validateAndExtractPublisherAuthority(row))
                   .withEmbargo(validateAndExtractEmbargo(row))
                   .withLicense(validateAndExtractLicense(row))
                   .withFilename(validateAndExtractFilename(row))
                   .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getCristinId() {
        return cristinId;
    }

    private void setCristinId(int cristinId) {
        this.cristinId = cristinId;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public PublisherAuthorityEnum getPublisherAuthority() {
        return publisherAuthority;
    }

    private void setPublisherAuthority(PublisherAuthorityEnum pubAuth) {
        this.publisherAuthority = pubAuth;
    }

    public Instant getEmbargo() {
        return embargo;
    }

    private void setEmbargo(Instant embargo) {
        this.embargo = embargo;
    }

    public BrageLicense getLicense() {
        return license;
    }

    private void setLicense(BrageLicense license) {
        this.license = license;
    }

    public String getFilename() {
        return filename;
    }

    private void setFilename(String filename) {
        this.filename = filename;
    }

    public Record toRecord() {
        var record = new Record();
        record.setId(UriWrapper.fromUri(DUMMY_HANDLE_THAT_EXIST_FOR_PROCESSING_UNIS + getCristinId()).getUri());
        record.setCristinId(Integer.toString(getCristinId()));
        record.setPublisherAuthority(getPublisherAuthority().toPublisherAuthority());
        record.setEntityDescription(new EntityDescription());
        record.setType(new Type(Set.of(CRISTIN_RECORD.getValue()), CRISTIN_RECORD.getValue()));
        record.setBrageLocation(EMPTY_STRING);
        record.setCustomer(new Customer(UNIS_CUSTOMER, null));

        var fileIdentifier = UUID.randomUUID();
        var contentFile = new ContentFile(
            getFilename(),
            BundleType.ORIGINAL,
            EMPTY_STRING,
            fileIdentifier,
            License.fromBrageLicense(getLicense()),
            getEmbargo());
        record.setContentBundle(new ResourceContent(contentFile));

        return record;
    }

    public static final class Builder {

        private final UnisContent unisContent;

        private Builder() {
            unisContent = new UnisContent();
        }

        public UnisContent build() throws InvalidUnisContentException {
            if (unisContent.getCristinId() == 0
                || isNull(unisContent.getTitle())
                || isNull(unisContent.getPublisherAuthority())
                || isNull(unisContent.getLicense())
                || isNull(unisContent.getFilename())) {
                throw new InvalidUnisContentException("Missing value(s)");
            }
            return unisContent;
        }

        public Builder withCristinId(int cristinId) {
            unisContent.setCristinId(cristinId);
            return this;
        }

        public Builder withTitle(String title) {
            unisContent.setTitle(title);
            return this;
        }

        public Builder withPublisherAuthority(PublisherAuthorityEnum pubAuth) {
            unisContent.setPublisherAuthority(pubAuth);
            return this;
        }

        public Builder withEmbargo(Instant embargo) {
            unisContent.setEmbargo(embargo);
            return this;
        }

        public Builder withLicense(BrageLicense license) {
            unisContent.setLicense(license);
            return this;
        }

        public Builder withFilename(String filename) {
            unisContent.setFilename(filename);
            return this;
        }
    }
}
