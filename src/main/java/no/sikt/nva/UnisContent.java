package no.sikt.nva;

import static java.util.Objects.isNull;
import static no.sikt.nva.validators.UnisContentValidator.validateAndExtractCristinId;
import static no.sikt.nva.validators.UnisContentValidator.validateAndExtractEmbargo;
import static no.sikt.nva.validators.UnisContentValidator.validateAndExtractFilename;
import static no.sikt.nva.validators.UnisContentValidator.validateAndExtractLicence;
import static no.sikt.nva.validators.UnisContentValidator.validateAndExtractPublisherAuthority;
import static no.sikt.nva.validators.UnisContentValidator.validateAndExtractTitle;
import static no.sikt.nva.validators.UnisContentValidator.validateRow;
import java.util.Date;
import no.sikt.nva.brage.migration.common.model.record.PublisherAuthorityEnum;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import no.sikt.nva.exceptions.ExcelException;
import no.sikt.nva.exceptions.InvalidUnisContentException;
import org.apache.poi.ss.usermodel.Row;

public final class UnisContent {
    private int cristinId;
    private String title;
    private PublisherAuthorityEnum publisherAuthority;
    private Date embargo;
    private BrageLicense licence;
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
                   .withLicence(validateAndExtractLicence(row))
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

    public Date getEmbargo() {
        return embargo;
    }

    private void setEmbargo(Date embargo) {
        this.embargo = embargo;
    }

    public BrageLicense getLicence() {
        return licence;
    }

    private void setLicence(BrageLicense licence) {
        this.licence = licence;
    }

    public String getFilename() {
        return filename;
    }

    private void setFilename(String filename) {
        this.filename = filename;
    }

    public Record toRecord() {
        return new Record();
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
                || isNull(unisContent.getLicence())
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

        public Builder withEmbargo(Date embargo) {
            unisContent.setEmbargo(embargo);
            return this;
        }

        public Builder withLicence(BrageLicense licence) {
            unisContent.setLicence(licence);
            return this;
        }

        public Builder withFilename(String filename) {
            unisContent.setFilename(filename);
            return this;
        }
    }
}
