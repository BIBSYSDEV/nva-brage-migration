package no.sikt.nva.model;

import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public class Embargo {

    private String handle;
    private String date;
    private String filename;

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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
