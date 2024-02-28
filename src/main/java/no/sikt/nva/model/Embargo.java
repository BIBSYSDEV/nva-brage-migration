package no.sikt.nva.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public class Embargo {

    public static final int MAX_EMBARGO_YEAR = 9999;
    private String handle;
    private String date;
    private String filename;

    private boolean detectedFile;

    public Embargo(String handle, String filename, String date) {
        this.handle = handle;
        this.filename = filename;
        this.date = date;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(handle, date, filename);
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Embargo embargo = (Embargo) o;
        return Objects.equals(handle, embargo.handle)
               && Objects.equals(date, embargo.date)
               && Objects.equals(filename, embargo.filename);
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isDetectedFile() {
        return detectedFile;
    }

    public void setDetectedFile(boolean detectedFile) {
        this.detectedFile = detectedFile;
    }

    public Instant getDateAsInstant() {
        try {
            var dateTime = ZonedDateTime.of(LocalDate.parse(date), LocalTime.now(), ZoneId.systemDefault());
            return formatEmbargoDate(dateTime);
        } catch (Exception e) {
            return perseFiveYearDateToInstant();
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private Instant perseFiveYearDateToInstant() {
        var fiveDigitYear = DateTimeFormatter.ofPattern("yyyyy-MM-dd");
        var dateTime = ZonedDateTime.of(LocalDate.parse(date, fiveDigitYear),
                                LocalTime.now(),
                                ZoneId.systemDefault());
        return formatEmbargoDate(dateTime);
    }

    private static Instant formatEmbargoDate(ZonedDateTime dateTime) {
        if (dateTime.getYear() > MAX_EMBARGO_YEAR) {
            return dateTime.withYear(MAX_EMBARGO_YEAR).toInstant();
        } else {
            return dateTime.toInstant();
        }
    }
}
