package no.sikt.nva.model;

import com.opencsv.bean.CsvBindByName;

public class TitleHandleBean {

    @CsvBindByName
    private String title;

    @CsvBindByName
    private String handle;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
}
