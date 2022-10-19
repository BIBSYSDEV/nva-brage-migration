package no.sikt.nva.scrapers;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;
import no.sikt.nva.model.TitleHandleBean;

public class HandleTitleMapReader {

    private static final String RESCUE_HANDLE_MAPPING_NVE = "nve_handles_for_datasets.csv";

    private final String mapFileName;

    public HandleTitleMapReader() {
        this(RESCUE_HANDLE_MAPPING_NVE);
    }

    public HandleTitleMapReader(String mapFileName) {
        this.mapFileName = mapFileName;
    }

    public Map<String, String> readNveTitleAndHandlesPatch() {
        try (

            var inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(mapFileName);
            var bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            HeaderColumnNameMappingStrategy<TitleHandleBean> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(TitleHandleBean.class);
            CsvToBean<TitleHandleBean> csvToBean = new CsvToBeanBuilder<TitleHandleBean>(bufferedReader)
                                                 .withMappingStrategy(strategy)
                                                 .build();
            return csvToBean.stream().collect(Collectors.toMap(TitleHandleBean::getTitle,
                                                               TitleHandleBean::getHandle));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
